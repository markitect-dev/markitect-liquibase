import com.diffplug.spotless.npm.PrettierFormatterStep
import com.github.gradle.node.variant.computeNodeDir
import com.github.gradle.node.variant.computeNodeExec

plugins {
    `kotlin-dsl`
    alias(libs.plugins.com.diffplug.spotless)
    alias(libs.plugins.com.github.node.gradle.node)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(plugin(libs.plugins.com.diffplug.spotless))
    implementation(plugin(libs.plugins.com.github.node.gradle.node))
    implementation(plugin(libs.plugins.com.github.spotbugs))
    implementation(plugin(libs.plugins.de.thetaphi.forbiddenapis))
    implementation(plugin(libs.plugins.net.ltgt.errorprone))
    implementation(plugin(libs.plugins.net.ltgt.nullaway))
    implementation(plugin(libs.plugins.org.openrewrite.rewrite))
    implementation(plugin(libs.plugins.org.sonarqube))
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

listOf("npmInstall", "npmSetup", "pnpmInstall", "pnpmSetup", "yarn", "yarnSetup").forEach {
    tasks.named(it) {
        enabled = false
    }
}

spotless {
    ratchetFrom("origin/main")
    kotlin {
        targetExclude("build/**", "src/**/*.gradle.kts")
        ktfmt(libs.versions.ktfmt.get())
    }
    kotlinGradle {
        target("src/**/*.gradle.kts", "*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
    format("properties") {
        target("gradle.properties")
        prettier(
            mapOf(
                "prettier" to PrettierFormatterStep.DEFAULT_VERSION,
                "prettier-plugin-properties" to libs.versions.prettier.plugin.properties.get(),
            ),
        )
            .nodeExecutable(computeNodeExec(node, computeNodeDir(node)))
            .npmInstallCache(rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache"))
            .config(
                mapOf(
                    "parser" to "dot-properties",
                    "plugins" to listOf("prettier-plugin-properties"),
                    "keySeparator" to "=",
                    "printWidth" to 0,
                ),
            )
    }
}

tasks.named("spotlessProperties") {
    dependsOn(rootProject.tasks.nodeSetup)
}

tasks.spotlessCheck {
    if (!providers.environmentVariable("CI").isPresent) {
        dependsOn(tasks.spotlessApply)
    }
}

@Suppress("UnusedReceiverParameter")
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { it.run { "$pluginId:$pluginId.gradle.plugin:$version" } }
