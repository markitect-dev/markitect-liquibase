plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    api(project(":markitect-liquibase-core"))
    api(libs.org.springframework.spring.context)
    implementation(libs.com.google.guava.guava)

    testImplementation(libs.org.springframework.spring.jdbc)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testRuntimeOnly(libs.com.h2database.h2)
}

description = "Markitect Liquibase Spring Integration"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.spring"
