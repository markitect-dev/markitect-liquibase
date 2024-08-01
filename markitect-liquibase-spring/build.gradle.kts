plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    api(project(":markitect-liquibase-core"))
    api(libs.org.springframework.spring.context)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.checkerframework.checker.qual)

    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.org.checkerframework.checker.qual)
    testImplementation(libs.org.springframework.spring.jdbc)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testRuntimeOnly(libs.com.h2database.h2)
}

description = "Markitect Liquibase Spring Integration"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.spring"
