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
    format("properties") {
        target("gradle.properties", "src/**/*.properties")
        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.asProvider().get(),
                "prettier-plugin-properties" to libs.versions.prettier.plugin.properties.get(),
            ),
        )
            .config(
                mapOf(
                    "parser" to "dot-properties",
                    "plugins" to listOf("prettier-plugin-properties"),
                    "keySeparator" to "=",
                    "printWidth" to 0,
                ),
            )
    }
    format("toml") {
        target("gradle/**/*.toml")
        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.asProvider().get(),
                "prettier-plugin-toml" to libs.versions.prettier.plugin.toml.get(),
            ),
        )
            .config(
                mapOf(
                    "parser" to "toml",
                    "plugins" to listOf("prettier-plugin-toml"),
                ),
            )
    }
    format("misc") {
        target(
            ".editorconfig",
            ".gitattributes",
            ".gitignore",
            ".java-version",
            ".sdkmanrc",
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
