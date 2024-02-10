plugins {
    id("buildlogic.common-conventions")
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
}

listOf("clean", "assemble", "check", "build", "spotlessApply", "spotlessCheck").forEach {
    tasks.named(it) {
        dependsOn(gradle.includedBuild("build-logic").task(":$it"))
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
