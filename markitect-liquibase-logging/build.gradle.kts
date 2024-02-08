plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    api(project(":markitect-liquibase-base"))
    api(libs.org.liquibase.liquibase.core)
    compileOnly(libs.biz.aqute.bnd.biz.aqute.bnd.annotation)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.apache.logging.log4j.log4j.api)
    compileOnly(libs.org.checkerframework.checker.qual)
    compileOnly(libs.org.osgi.org.osgi.annotation.bundle)
    compileOnly(libs.org.osgi.org.osgi.annotation.versioning)
    compileOnly(libs.org.slf4j.slf4j.api)

    testCompileOnly(libs.biz.aqute.bnd.biz.aqute.bnd.annotation)
    testCompileOnly(libs.org.osgi.org.osgi.annotation.bundle)
    testCompileOnly(libs.org.osgi.org.osgi.annotation.versioning)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

description = "Markitect Liquibase Logging"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.logging"
