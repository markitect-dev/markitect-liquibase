plugins {
    id("com.diffplug.spotless")
}

spotless {
    plugins.withId("java") {
        java {
            targetExclude("build/**")
            licenseHeaderFile(rootProject.file("config/spotless/license-header-java"))
            googleJavaFormat(libs.versions.google.java.format.get())
        }
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    format("misc") {
        target(
            "config/**/*.xml",
            "gradle/**/*.toml",
            "src/**/*.properties",
            "src/**/*.xml",
            "src/**/*.yaml",
            "src/**/*.yml",
            ".editorconfig",
            ".java-version",
            ".sdkmanrc",
            "gradle.properties",
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
