@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.enterprise") version "3.16.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "markitect-liquibase-build"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

gradleEnterprise {
    if (providers.environmentVariable("CI").isPresent) {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
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
