@file:Suppress("UnstableApiUsage")

pluginManagement {
    val foojayResolverVersion =
        providers.fileContents(layout.settingsDirectory.file("../gradle/libs.versions.toml")).asText.map { text ->
            val versionsSection =
                Regex("""(?ms)^\s*\[versions]\s*$\s*(.*?)(?:^\s*\[|\z)""")
                    .find(text)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: error("Could not find [versions] section in '../gradle/libs.versions.toml'")
            Regex("""(?m)^\s*foojay-resolver\s*=\s*(['\"])([^'\"]+)\1\s*(?:#.*)?$""")
                .find(versionsSection)
                ?.groupValues
                ?.getOrNull(2)
                ?: error("Could not find 'foojay-resolver' version in [versions] in '../gradle/libs.versions.toml'")
        }.get()
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        mavenCentral {
            content {
                excludeModule("org.nodejs", "node")
            }
        }
        gradlePluginPortal {
            content {
                excludeModule("org.nodejs", "node")
            }
        }
        ivy {
            url = uri("https://nodejs.org/dist/")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
