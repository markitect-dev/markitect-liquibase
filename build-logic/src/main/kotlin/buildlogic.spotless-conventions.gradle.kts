plugins {
    id("buildlogic.node-conventions")
    id("com.diffplug.spotless")
}

val ci = providers.environmentVariable("CI").isPresent
val windows = providers.systemProperty("os.name").get().startsWith("Windows", ignoreCase = true)
val nodeExecutable = tasks.nodeSetup.map { it.nodeDir.get().file(if (windows) "node.exe" else "bin/node") }
val npmExecutable = tasks.nodeSetup.map { it.nodeDir.get().file(if (windows) "npm.cmd" else "bin/npm") }
val npmInstallCache = rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache")
val npmrc = rootProject.file("config/spotless/.npmrc")

spotless {
    ratchetFrom("origin/main")
    plugins.withId("java") {
        java {
            target("src/**/*.java")
            licenseHeaderFile(rootProject.file("config/spotless/license-header-java"))
            cleanthat()
                .version(libs.versions.cleanthat.get())
                .sourceCompatibility("17")
                .addMutator("SafeButNotConsensual")
                .addMutator("UnnecessarySemicolon")
                .excludeMutator("AvoidInlineConditionals")
                .excludeMutator("LambdaIsMethodReference")
                .excludeMutator("LiteralsFirstInComparisons")
                .excludeMutator("LocalVariableTypeInference")
            googleJavaFormat(libs.versions.google.java.format.get())
                .reflowLongStrings()
        }
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    json {
        target("renovate.json5")
        prettier(libs.versions.prettier.asProvider().get())
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
            .npmInstallCache(npmInstallCache)
            .npmrc(npmrc)
            .config(
                mapOf(
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
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
            .npmInstallCache(npmInstallCache)
            .npmrc(npmrc)
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
            .nodeExecutable(nodeExecutable)
            .npmExecutable(npmExecutable)
            .npmInstallCache(npmInstallCache)
            .npmrc(npmrc)
            .config(
                mapOf(
                    "parser" to "toml",
                    "plugins" to listOf("prettier-plugin-toml"),
                ),
            )
    }
    format("misc") {
        target(
            "config/**/.npmrc",
            "config/**/*.xml",
            "src/**/*.xml",
            "src/**/*.yaml",
            "src/**/*.yml",
            ".editorconfig",
            ".java-version",
            ".sdkmanrc",
        )
        leadingTabsToSpaces(2)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

listOf("spotlessJson", "spotlessProperties", "spotlessToml").forEach { name ->
    tasks.named(name) {
        dependsOn(rootProject.tasks.nodeSetup)
    }
}

tasks.spotlessCheck {
    if (!ci) {
        dependsOn(tasks.spotlessApply)
    }
}
