plugins {
    jacoco
    `java-library`
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    enabled = false
}
