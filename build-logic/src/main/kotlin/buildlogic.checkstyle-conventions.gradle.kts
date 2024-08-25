plugins {
    checkstyle
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configProperties =
        mapOf(
            "org.checkstyle.google.suppressionfilter.config" to
                configDirectory.file("suppressions.xml").get().asFile.path,
            "org.checkstyle.google.suppressionxpathfilter.config" to
                configDirectory.file("xpath-suppressions.xml").get().asFile.path,
        )
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    group = "verification"
}
