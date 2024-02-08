plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            plugins.withId("java-library") {
                from(components["java"])
            }
            plugins.withId("java-platform") {
                from(components["javaPlatform"])
            }
            pom {
                name = artifactId
                description = providers.provider { project.description }
                url = "https://github.com/markitect-dev/markitect-liquibase"
                inceptionYear = "2023"
                organization {
                    name = "Markitect"
                    url = "https://github.com/markitect-dev"
                }
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        name = "Mark Chesney"
                        url = "https://github.com/mches"
                        organization = "Markitect"
                        organizationUrl = "https://github.com/markitect-dev"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/markitect-dev/markitect-liquibase.git"
                    developerConnection = "scm:git:ssh://git@github.com/markitect-dev/markitect-liquibase.git"
                    url = "https://github.com/markitect-dev/markitect-liquibase/tree/main"
                }
            }
        }
    }
    repositories {
        val isSnapshot = providers.provider { project.version.toString().endsWith("-SNAPSHOT") }
        val mavenAllowInsecureProtocol = providers.gradleProperty("mavenAllowInsecureProtocol").map(String::toBoolean)
        val mavenReleaseRepositoryUrl = providers.gradleProperty("mavenReleaseRepositoryUrl").map(::uri)
        val mavenSnapshotRepositoryUrl = providers.gradleProperty("mavenSnapshotRepositoryUrl").map(::uri)
        val mavenRepositoryUrl = if (isSnapshot.get()) mavenSnapshotRepositoryUrl else mavenReleaseRepositoryUrl
        if (mavenRepositoryUrl.isPresent) {
            maven {
                name = "maven"
                url = mavenRepositoryUrl.get()
                isAllowInsecureProtocol = mavenAllowInsecureProtocol.getOrElse(isAllowInsecureProtocol)
                credentials(PasswordCredentials::class)
            }
        }
    }
}

signing {
    val signingKeyId = providers.gradleProperty("signingKeyId")
    val signingKey = providers.gradleProperty("signingKey")
    val signingPassword = providers.gradleProperty("signingPassword")
    if (signingKey.isPresent && signingPassword.isPresent) {
        useInMemoryPgpKeys(signingKeyId.orNull, signingKey.get(), signingPassword.get())
    } else {
        useGpgCmd()
    }
    sign(publishing.publications["maven"])
}
