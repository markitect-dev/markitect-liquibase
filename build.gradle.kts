plugins {
    id("buildlogic.common-conventions")
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
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
