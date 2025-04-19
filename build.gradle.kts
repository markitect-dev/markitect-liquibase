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
        val sonatypeUsername = providers.gradleProperty("sonatypeUsername")
        val sonatypePassword = providers.gradleProperty("sonatypePassword")
        if (sonatypeUsername.isPresent && sonatypePassword.isPresent) {
            sonatype {
                nexusUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}
