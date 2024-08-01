plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.org.checkerframework.checker.qual)
    testImplementation(project(":markitect-liquibase-logging"))
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

description = "Markitect Liquibase Logging - SLF4J SPI Tests"
