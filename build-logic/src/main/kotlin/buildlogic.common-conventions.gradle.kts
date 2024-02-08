plugins {
    id("buildlogic.spotless-conventions")
    id("buildlogic.sonar-conventions")
}

group = "dev.markitect.liquibase"
version = providers.gradleProperty("version").get()
