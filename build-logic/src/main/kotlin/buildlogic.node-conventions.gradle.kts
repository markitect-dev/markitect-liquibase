plugins {
    id("com.github.node-gradle.node")
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
