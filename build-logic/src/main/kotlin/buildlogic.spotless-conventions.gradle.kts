plugins {
    id("com.diffplug.spotless")
}

spotless {
    ratchetFrom = "origin/main"
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        licenseHeaderFile(rootProject.file("config/spotless/license-header-java"))
        googleJavaFormat(libs.versions.google.java.format.get())
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    yaml {
        target("src/**/*.yaml", "src/**/*.yml")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target(
            ".editorconfig",
            ".gitattributes",
            ".gitignore",
            ".java-version",
            ".sdkmanrc",
            "gradle.properties",
            "gradle/**/*.toml",
            "src/**/*.properties",
            "src/**/*.xml",
        )
        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(2)
    }
}

tasks.spotlessCheck {
    if (!providers.environmentVariable("CI").isPresent) {
        dependsOn(tasks.spotlessApply)
    }
}
