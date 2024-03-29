plugins {
    id("buildlogic.java-conventions")
}

configurations.testImplementation {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
    testImplementation(project(":markitect-liquibase-logging"))
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testRuntimeOnly(libs.org.springframework.boot.spring.boot.starter.log4j2)
}

description = "Markitect Liquibase Logging - Log4j Tests"
