plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    api(libs.org.liquibase.liquibase.core)
    compileOnly(libs.biz.aqute.bnd.biz.aqute.bnd.annotation)
    compileOnly(libs.org.apache.logging.log4j.log4j.api)
    compileOnly(libs.org.osgi.org.osgi.annotation.bundle)
    compileOnly(libs.org.osgi.org.osgi.annotation.versioning)
    compileOnly(libs.org.slf4j.slf4j.api)
    implementation(libs.com.google.guava.guava)

    testCompileOnly(libs.biz.aqute.bnd.biz.aqute.bnd.annotation)
    testCompileOnly(libs.org.osgi.org.osgi.annotation.bundle)
    testCompileOnly(libs.org.osgi.org.osgi.annotation.versioning)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

description = "Markitect Liquibase Logging"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.logging"
