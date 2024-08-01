plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    annotationProcessor(libs.org.springframework.boot.spring.boot.configuration.processor)

    api(project(":markitect-liquibase-spring"))
    api(libs.org.springframework.boot.spring.boot.starter.jdbc)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.checkerframework.checker.qual)

    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.org.checkerframework.checker.qual)
    testImplementation(libs.com.h2database.h2)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

description = "Markitect Liquibase Spring Boot Starter"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.spring.boot.starter"
