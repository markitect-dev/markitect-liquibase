@file:Suppress("UnstableApiUsage")

import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    id("buildlogic.common-conventions")
    id("buildlogic.checkstyle-conventions")
    id("buildlogic.forbiddenapis-conventions")
    id("buildlogic.jacoco-conventions")
    id("buildlogic.rewrite-conventions")
    id("buildlogic.spotbugs-conventions")
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
    `java-library`
}

val ci = providers.environmentVariable("CI").isPresent
val epPatchChecks = findProperty("epPatchChecks") as String?
val epPatchLocation = (findProperty("epPatchLocation") as String?) ?: "IN_PLACE"
val skipTests = providers.systemProperty("skipTests").isPresent
val testProject = project.name.endsWith("-test")

dependencies {
    errorprone(libs.com.google.errorprone.error.prone.core)
    errorprone(libs.com.uber.nullaway.nullaway)

    annotationProcessor(platform(libs.org.springframework.boot.spring.boot.dependencies))
    api(platform(libs.org.springframework.boot.spring.boot.dependencies))
    compileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    compileOnly(libs.de.thetaphi.forbiddenapis)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.jspecify.jspecify)

    testCompileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.de.thetaphi.forbiddenapis)
    testCompileOnly(libs.org.jspecify.jspecify)

    constraints {
        annotationProcessor(libs.com.google.guava.guava)
        annotationProcessor(libs.com.opencsv.opencsv)
        annotationProcessor(libs.commons.beanutils.commons.beanutils)
        annotationProcessor(libs.commons.io.commons.io)
        annotationProcessor(libs.info.picocli.picocli)
        annotationProcessor(libs.javax.activation.javax.activation.api)
        annotationProcessor(libs.javax.xml.bind.jaxb.api)
        annotationProcessor(libs.org.apache.commons.commons.collections4)
        annotationProcessor(libs.org.apache.commons.commons.lang3)
        annotationProcessor(libs.org.apache.commons.commons.text)
        annotationProcessor(libs.org.liquibase.liquibase.cdi)
        annotationProcessor(libs.org.liquibase.liquibase.core)
        api(libs.com.google.guava.guava)
        api(libs.com.opencsv.opencsv)
        api(libs.commons.beanutils.commons.beanutils)
        api(libs.commons.io.commons.io)
        api(libs.info.picocli.picocli)
        api(libs.javax.activation.javax.activation.api)
        api(libs.javax.xml.bind.jaxb.api)
        api(libs.org.apache.commons.commons.collections4)
        api(libs.org.apache.commons.commons.lang3)
        api(libs.org.apache.commons.commons.text)
        api(libs.org.liquibase.liquibase.cdi)
        api(libs.org.liquibase.liquibase.core)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
        vendor = JvmVendorSpec.AZUL
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    if (!ci) {
        outputs.upToDateWhen { false }
    }
    options.compilerArgs.add("-parameters")
    if (ci) {
        options.compilerArgs.add("-Werror")
    }
    options.compilerArgs.add("-Xlint:all,-classfile,-processing")
    options.errorprone {
        if (!ci) {
            allDisabledChecksAsWarnings = true
            allSuggestionsAsWarnings = true
            disable("AddNullMarkedToClass")
            disable("Java8ApiChecker")
            disable("SuppressWarningsWithoutExplanation")
            warn("RequireExplicitNullMarking")
            if (epPatchChecks != null) {
                errorproneArgs.add("-XepPatchChecks:$epPatchChecks")
                errorproneArgs.add("-XepPatchLocation:$epPatchLocation")
            }
        }
        nullaway {
            excludedFieldAnnotations.add("org.junit.jupiter.api.io.TempDir")
            excludedFieldAnnotations.add("org.mockito.Captor")
            excludedFieldAnnotations.add("org.mockito.InjectMocks")
            excludedFieldAnnotations.add("org.mockito.Mock")
            excludedFieldAnnotations.add("org.springframework.test.context.bean.override.mockito.MockitoBean")
            warn()
        }
    }
    options.release = 17
}

testing.suites.withType<JvmTestSuite>().configureEach {
    useJUnitJupiter()
    targets.all {
        testTask.configure {
            onlyIf { !skipTests }
            environment("LIQUIBASE_ANALYTICS_ENABLED", "false")
            environment("LIQUIBASE_ANALYTICS_LOG_LEVEL", "info")
            environment("LIQUIBASE_SHOW_BANNER", "false")
            environment("LIQUIBASE_SQL_LOG_LEVEL", "info")
        }
    }
}

tasks.jar {
    onlyIf { !testProject }
    if (project.hasProperty("automaticModuleName")) {
        manifest {
            attributes(mapOf("Automatic-Module-Name" to project.property("automaticModuleName")))
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
    onlyIf { !testProject }
}

tasks.named<Jar>("sourcesJar") {
    onlyIf { !testProject }
    metaInf {
        from(rootProject.file("LICENSE.txt"))
        from(rootProject.file("NOTICE.txt"))
    }
}

nullaway {
    onlyNullMarked = true
}
