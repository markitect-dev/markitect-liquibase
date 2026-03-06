plugins {
    id("buildlogic.common-conventions")
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
}

val cleanthat by configurations.registering {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

val googleJavaFormat by configurations.registering {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

val ktfmt by configurations.registering {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

val ktlint by configurations.registering {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

val node by configurations.registering {
    isCanBeConsumed = false
}

dependencies {
    cleanthat(libs.io.github.solven.eu.cleanthat.java)

    googleJavaFormat(libs.com.google.googlejavaformat.google.java.format)

    ktfmt(libs.com.facebook.ktfmt)

    ktlint(libs.com.pinterest.ktlint.ktlint.cli)

    node("org.nodejs:node:${libs.versions.node.get()}:darwin-arm64@tar.gz")
    node("org.nodejs:node:${libs.versions.node.get()}:darwin-x64@tar.gz")
    node("org.nodejs:node:${libs.versions.node.get()}:linux-arm64@tar.gz")
    node("org.nodejs:node:${libs.versions.node.get()}:linux-x64@tar.gz")
    node("org.nodejs:node:${libs.versions.node.get()}:win-arm64@zip")
    node("org.nodejs:node:${libs.versions.node.get()}:win-x64@zip")
}

listOf("check", "spotlessApply", "spotlessCheck").forEach { name ->
    tasks.named(name) {
        dependsOn(gradle.includedBuild("build-logic").task(":$name"))
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
            snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".idea"))
    }
}
