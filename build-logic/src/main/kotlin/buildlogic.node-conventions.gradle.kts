plugins {
    id("com.github.node-gradle.node")
}

val verification: Configuration by configurations.creating

dependencies {
    verification("org.nodejs:node:${libs.versions.node.get()}:darwin-arm64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:darwin-x64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:linux-arm64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:linux-x64@tar.gz")
    verification("org.nodejs:node:${libs.versions.node.get()}:win-arm64@zip")
    verification("org.nodejs:node:${libs.versions.node.get()}:win-x64@zip")
}

node {
    workDir = rootProject.layout.projectDirectory.dir(".gradle/nodejs")
    version = libs.versions.node.get()
    distBaseUrl = null
    download = true
    enableTaskRules = false
}

tasks.nodeSetup {
    enabled = project == rootProject
}

listOf("npmInstall", "npmSetup", "pnpmInstall", "pnpmSetup", "yarn", "yarnSetup").forEach { name ->
    tasks.named(name) {
        enabled = false
    }
}
