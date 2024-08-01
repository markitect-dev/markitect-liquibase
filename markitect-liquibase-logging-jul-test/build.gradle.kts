plugins {
    id("buildlogic.java-conventions")
}

configurations.testImplementation {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.org.checkerframework.checker.qual)
    testImplementation(project(":markitect-liquibase-logging"))
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testRuntimeOnly(libs.org.apache.logging.log4j.log4j.to.slf4j)
    testRuntimeOnly(libs.org.slf4j.slf4j.jdk14)
}

description = "Markitect Liquibase Logging - JUL Tests"
