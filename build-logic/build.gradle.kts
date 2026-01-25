import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.com.diffplug.spotless)
    alias(libs.plugins.com.github.node.gradle.node)
    idea
}

val ci = providers.environmentVariable("CI").isPresent
val windows = providers.systemProperty("os.name").get().startsWith("Windows", ignoreCase = true)
val nodeExecutable = tasks.nodeSetup.map { it.nodeDir.get().file(if (windows) "node.exe" else "bin/node") }
val npmExecutable = tasks.nodeSetup.map { it.nodeDir.get().file(if (windows) "npm.cmd" else "bin/npm") }
val npmInstallCache = rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache")
val npmrc = rootProject.file("../config/spotless/.npmrc")

val verification: Configuration by configurations.creating

dependencies {
    verification("org.nodejs:node:${libs.versions.node.get()}:darwin-arm64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:darwin-x64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:linux-arm64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:linux-x64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:win-arm64@zip")
    verification("org.nodejs:node:${libs.versions.node.get()}:win-x64@zip")

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(plugin(libs.plugins.com.diffplug.spotless))
    implementation(plugin(libs.plugins.com.github.node.gradle.node))
    implementation(plugin(libs.plugins.com.github.spotbugs))
    implementation(plugin(libs.plugins.com.gradle.develocity))
    implementation(plugin(libs.plugins.de.thetaphi.forbiddenapis))
    implementation(plugin(libs.plugins.net.ltgt.errorprone))
    implementation(plugin(libs.plugins.net.ltgt.nullaway))
    implementation(plugin(libs.plugins.org.gradle.toolchains.foojay.resolver.convention))
    implementation(plugin(libs.plugins.org.openrewrite.rewrite))
    implementation(plugin(libs.plugins.org.sonarqube))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

node {
    workDir = rootProject.layout.projectDirectory.dir(".gradle/nodejs")
    version = libs.versions.node.get()
    distBaseUrl = null
    download = true
    enableTaskRules = false
}

tasks.nodeSetup {
    enabled = project == rootProject
}

listOf("npmInstall", "npmSetup", "pnpmInstall", "pnpmSetup", "yarn", "yarnSetup").forEach { name ->
    tasks.named(name) {
        enabled = false
    }
}

spotless {
    ratchetFrom("origin/main")
    kotlin {
        target(
            "src/**/*.kt",
            "src/**/*.kts",
        )
        targetExclude("src/**/*.gradle.kts")
        ktfmt(libs.versions.ktfmt.get())
    }
    kotlinGradle {
        target(
            "src/**/*.gradle.kts",
            "*.gradle.kts",
        )
        ktlint(libs.versions.ktlint.get())
    }
    format("properties") {
        target("gradle.properties")
        replaceRegex("prePrettier", "(?m)^([ \\t]*[#!])", "# $1")
        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.asProvider().get(),
                "prettier-plugin-properties" to libs.versions.prettier.plugin.properties.get(),
            ),
        )
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
            .npmInstallCache(npmInstallCache)
            .npmrc(npmrc)
            .config(
                mapOf(
                    "parser" to "dot-properties",
                    "plugins" to listOf("prettier-plugin-properties"),
                    "keySeparator" to "=",
                    "printWidth" to 0,
                ),
            )
        replaceRegex("postPrettier", "(?m)^# ", "")
    }
}

tasks.named("spotlessProperties") {
    dependsOn(rootProject.tasks.nodeSetup)
}

if (!ci) {
    tasks.spotlessCheck {
        dependsOn(tasks.spotlessApply)
    }
}

idea {
    module {
        excludeDirs.add(file(".kotlin"))
    }
}

@Suppress("UnusedReceiverParameter")
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
