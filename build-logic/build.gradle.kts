plugins {
    `kotlin-dsl`
    alias(libs.plugins.com.diffplug.spotless)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(plugin(libs.plugins.com.diffplug.spotless))
    implementation(plugin(libs.plugins.net.ltgt.errorprone))
    implementation(plugin(libs.plugins.net.ltgt.nullaway))
    implementation(plugin(libs.plugins.org.sonarqube))
}

spotless {
    ratchetFrom = "origin/main"
    kotlin {
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("*.gradle.kts", "src/main/kotlin/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

@Suppress("UnusedReceiverParameter")
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { it.run { "$pluginId:$pluginId.gradle.plugin:$version" } }
