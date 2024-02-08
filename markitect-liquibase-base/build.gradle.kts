plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.checkerframework.checker.qual)

    testImplementation(libs.org.assertj.assertj.core)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testImplementation(libs.org.mockito.mockito.core)
    testImplementation(libs.org.mockito.mockito.junit.jupiter)
}

description = "Markitect Liquibase Base"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.base"
