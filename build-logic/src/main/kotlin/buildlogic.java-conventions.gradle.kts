@file:Suppress("UnstableApiUsage")

plugins {
    id("buildlogic.common-conventions")
    id("buildlogic.checkstyle-conventions")
    id("buildlogic.errorprone-conventions")
    id("buildlogic.forbiddenapis-conventions")
    id("buildlogic.jacoco-conventions")
    id("buildlogic.rewrite-conventions")
    id("buildlogic.spotbugs-conventions")
    `java-library`
}

val mockitoAgent: Configuration by configurations.creating

configurations.testImplementation {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.hamcrest:hamcrest-core"))
            .using(module(libs.org.hamcrest.hamcrest.get().toString()))
    }
    exclude(group = "com.jayway.jsonpath", module = "json-path")
    exclude(group = "javax.xml.bind", module = "jaxb-api")
    exclude(group = "net.minidev", module = "json-smart")
    exclude(group = "org.skyscreamer", module = "jsonassert")
    exclude(group = "org.xmlunit", module = "xmlunit-core")
}

dependencies {
    mockitoAgent(platform(libs.org.mockito.mockito.bom))
    mockitoAgent(libs.org.mockito.mockito.core) { isTransitive = false }

    compileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    compileOnly(libs.de.thetaphi.forbiddenapis)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.jspecify.jspecify)

    testCompileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.de.thetaphi.forbiddenapis)
    testCompileOnly(libs.org.jspecify.jspecify)
    testImplementation(platform(libs.com.fasterxml.jackson.jackson.bom))
    testImplementation(platform(libs.io.micrometer.micrometer.bom))
    testImplementation(platform(libs.org.assertj.assertj.bom))
    testImplementation(platform(libs.org.junit.junit.bom))
    testImplementation(platform(libs.org.mockito.mockito.bom))
    testImplementation(platform(libs.org.springframework.spring.framework.bom))
    testImplementation(platform(libs.org.testcontainers.testcontainers.bom))

    constraints {
        testImplementation(libs.biz.aqute.bnd.biz.aqute.bnd.annotation)
        testImplementation(libs.ch.qos.logback.logback.classic)
        testImplementation(libs.ch.qos.logback.logback.core)
        testImplementation(libs.com.github.docker.java.docker.java.api)
        testImplementation(libs.com.github.docker.java.docker.java.transport)
        testImplementation(libs.com.github.docker.java.docker.java.transport.zerodep)
        testImplementation(libs.com.github.spotbugs.spotbugs.annotations)
        testImplementation(libs.com.google.errorprone.error.prone.annotations)
        testImplementation(libs.com.h2database.h2)
        testImplementation(libs.com.microsoft.sqlserver.mssql.jdbc)
        testImplementation(libs.com.opencsv.opencsv)
        testImplementation(libs.com.zaxxer.hikaricp)
        testImplementation(libs.commons.codec.commons.codec)
        testImplementation(libs.commons.io.commons.io)
        testImplementation(libs.io.r2dbc.r2dbc.spi)
        testImplementation(libs.jakarta.activation.jakarta.activation.api)
        testImplementation(libs.jakarta.annotation.jakarta.annotation.api)
        testImplementation(libs.jakarta.xml.bind.jakarta.xml.bind.api)
        testImplementation(libs.junit.junit)
        testImplementation(libs.net.bytebuddy.byte.buddy)
        testImplementation(libs.net.bytebuddy.byte.buddy.agent)
        testImplementation(libs.net.java.dev.jna.jna)
        testImplementation(libs.org.apache.commons.commons.collections4)
        testImplementation(libs.org.apache.commons.commons.compress)
        testImplementation(libs.org.apache.commons.commons.lang3)
        testImplementation(libs.org.apache.commons.commons.text)
        testImplementation(libs.org.apache.logging.log4j.log4j.api)
        testImplementation(libs.org.apache.logging.log4j.log4j.core)
        testImplementation(libs.org.apache.logging.log4j.log4j.jul)
        testImplementation(libs.org.apache.logging.log4j.log4j.slf4j2.impl)
        testImplementation(libs.org.apache.logging.log4j.log4j.to.slf4j)
        testImplementation(libs.org.apiguardian.apiguardian.api)
        testImplementation(libs.org.awaitility.awaitility)
        testImplementation(libs.org.hamcrest.hamcrest)
        testImplementation(libs.org.hsqldb.hsqldb)
        testImplementation(libs.org.jetbrains.annotations)
        testImplementation(libs.org.jooq.jooq)
        testImplementation(libs.org.jspecify.jspecify)
        testImplementation(libs.org.junit.pioneer.junit.pioneer)
        testImplementation(libs.org.liquibase.liquibase.core)
        testImplementation(libs.org.objenesis.objenesis)
        testImplementation(libs.org.opentest4j.opentest4j)
        testImplementation(libs.org.osgi.org.osgi.annotation.bundle)
        testImplementation(libs.org.osgi.org.osgi.annotation.versioning)
        testImplementation(libs.org.osgi.org.osgi.resource)
        testImplementation(libs.org.osgi.org.osgi.service.serviceloader)
        testImplementation(libs.org.postgresql.postgresql)
        testImplementation(libs.org.reactivestreams.reactive.streams)
        testImplementation(libs.org.rnorth.duct.tape.duct.tape)
        testImplementation(libs.org.slf4j.jul.to.slf4j)
        testImplementation(libs.org.slf4j.slf4j.api)
        testImplementation(libs.org.slf4j.slf4j.jdk14)
        testImplementation(libs.org.slf4j.slf4j.simple)
        testImplementation(libs.org.springframework.boot.spring.boot)
        testImplementation(libs.org.springframework.boot.spring.boot.autoconfigure)
        testImplementation(libs.org.springframework.boot.spring.boot.starter)
        testImplementation(libs.org.springframework.boot.spring.boot.starter.jdbc)
        testImplementation(libs.org.springframework.boot.spring.boot.starter.jooq)
        testImplementation(libs.org.springframework.boot.spring.boot.starter.log4j2)
        testImplementation(libs.org.springframework.boot.spring.boot.starter.logging)
        testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
        testImplementation(libs.org.springframework.boot.spring.boot.test)
        testImplementation(libs.org.springframework.boot.spring.boot.test.autoconfigure)
        testImplementation(libs.org.yaml.snakeyaml)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.AZUL
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    if (!providers.environmentVariable("CI").isPresent) {
        outputs.upToDateWhen { false }
    }
    options.compilerArgs.add("-parameters")
    if (providers.environmentVariable("CI").isPresent) {
        options.compilerArgs.add("-Werror")
    }
    options.compilerArgs.add("-Xlint:all,-processing")
    options.release = 17
}

testing.suites.withType<JvmTestSuite>().configureEach {
    useJUnitJupiter(libs.versions.junit.jupiter.get())
    targets.all {
        testTask.configure {
            onlyIf { !providers.systemProperty("skipTests").isPresent }
            environment("LIQUIBASE_ANALYTICS_ENABLED", "false")
            environment("LIQUIBASE_ANALYTICS_LOG_LEVEL", "info")
            environment("LIQUIBASE_SHOW_BANNER", "false")
            environment("LIQUIBASE_SQL_LOG_LEVEL", "info")
            jvmArgs("-javaagent:${mockitoAgent.asPath}")
            jvmArgs("-XX:-EnableDynamicAgentLoading")
        }
    }
}

tasks.jar {
    onlyIf { !project.name.endsWith("-test") }
    if (project.hasProperty("automaticModuleName")) {
        manifest {
            attributes("Automatic-Module-Name" to project.property("automaticModuleName"))
        }
    }
    metaInf {
        from(rootProject.file("LICENSE.txt"))
        from(rootProject.file("NOTICE.txt"))
    }
}

tasks.javadoc {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

tasks.named<Jar>("javadocJar") {
    onlyIf { !project.name.endsWith("-test") }
}

tasks.named<Jar>("sourcesJar") {
    onlyIf { !project.name.endsWith("-test") }
    metaInf {
        from(rootProject.file("LICENSE.txt"))
        from(rootProject.file("NOTICE.txt"))
    }
}
