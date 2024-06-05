plugins {
    id("com.diffplug.spotless")
}

spotless {
    ratchetFrom("origin/main")
    plugins.withId("java") {
        java {
            targetExclude("build/**")
            licenseHeaderFile(rootProject.file("config/spotless/license-header-java"))
            googleJavaFormat(libs.versions.google.java.format.get())
        }
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "ktlint_official",
                ),
            )
    }
    format("json5") {
        target("renovate.json5")
        prettier(libs.versions.prettier.asProvider().get())
            .config(
                mapOf(
                    "parser" to "json5",
                    "singleQuote" to true,
                ),
            )
    }
    format("properties") {
        target("src/**/*.properties", "gradle.properties")
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
            "config/**/*.xml",
            "src/**/*.xml",
            "src/**/*.yaml",
            "src/**/*.yml",
            ".editorconfig",
            ".java-version",
            ".sdkmanrc",
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
