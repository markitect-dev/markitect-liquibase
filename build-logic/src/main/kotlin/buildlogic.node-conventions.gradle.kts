plugins {
    id("com.github.node-gradle.node")
}

node {
    workDir = rootProject.layout.projectDirectory.dir(".gradle/nodejs")
    version = libs.versions.node.get()
    distBaseUrl = null
    download = true
}
