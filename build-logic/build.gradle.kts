import com.diffplug.gradle.spotless.SpotlessTask
import com.github.gradle.node.task.NodeSetupTask
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
    implementation(plugin(libs.plugins.org.sonarqube))
}

node {
    version = libs.versions.node.get()
    distBaseUrl = null
    download = true
}

spotless {
    ratchetFrom("origin/main")
    kotlin {
        targetExclude("build/**")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    format("properties") {
        target("gradle.properties")
        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.asProvider().get(),
                "prettier-plugin-properties" to libs.versions.prettier.plugin.properties.get(),
            ),
        )
            .nodeExecutable(computeNodeExec(node, computeNodeDir(node)))
            .npmInstallCache()
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

tasks.named<SpotlessTask>("spotlessProperties") {
    dependsOn(tasks.named<NodeSetupTask>("nodeSetup"))
}

tasks.spotlessCheck {
    if (!providers.environmentVariable("CI").isPresent) {
        dependsOn(tasks.spotlessApply)
    }
}

@Suppress("UnusedReceiverParameter")
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { it.run { "$pluginId:$pluginId.gradle.plugin:$version" } }
