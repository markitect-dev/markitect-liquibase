@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.develocity") version "3.17.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "markitect-liquibase-build"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

develocity {
    buildScan {
        if (providers.environmentVariable("CI").isPresent) {
            termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
            termsOfUseAgree.set("yes")
        }
        publishing.onlyIf { false }
    }
}

include(":markitect-liquibase-base")
include(":markitect-liquibase-bom")
include(":markitect-liquibase-core")
include(":markitect-liquibase-coverage")
include(":markitect-liquibase-logging")
include(":markitect-liquibase-logging-jul-tests")
include(":markitect-liquibase-logging-log4j-tests")
include(":markitect-liquibase-logging-slf4j-api-tests")
include(":markitect-liquibase-logging-slf4j-spi-tests")
include(":markitect-liquibase-spring")
include(":markitect-liquibase-spring-boot-starter")
