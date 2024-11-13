@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.develocity") version "3.18.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "markitect-liquibase-build"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        ivy {
            name = "Node.js"
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
}

develocity {
    buildScan {
        if (providers.environmentVariable("CI").isPresent) {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
        }
        publishing.onlyIf { false }
    }
}

include(":markitect-liquibase-base")
include(":markitect-liquibase-bom")
include(":markitect-liquibase-core")
include(":markitect-liquibase-coverage")
include(":markitect-liquibase-logging")
include(":markitect-liquibase-logging-jul-test")
include(":markitect-liquibase-logging-log4j-test")
include(":markitect-liquibase-logging-slf4j-api-test")
include(":markitect-liquibase-logging-slf4j-spi-test")
include(":markitect-liquibase-spring")
include(":markitect-liquibase-spring-boot-starter")
