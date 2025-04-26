import com.github.gradle.node.variant.computeNodeDir
import com.github.gradle.node.variant.computeNodeExec

plugins {
    id("buildlogic.node-conventions")
    id("com.diffplug.spotless")
}

val ci = providers.environmentVariable("CI").isPresent

spotless {
    ratchetFrom("origin/main")
    plugins.withId("java") {
        java {
            targetExclude("build/**")
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
            googleJavaFormat(libs.versions.google.java.format.get()).reflowLongStrings()
        }
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    format("json5") {
        target("renovate.json5")
        prettier(libs.versions.prettier.asProvider().get())
            .nodeExecutable(computeNodeExec(node, computeNodeDir(node)))
            .npmInstallCache(rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache"))
            .npmrc(rootProject.file("config/spotless/.npmrc"))
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
            .nodeExecutable(computeNodeExec(node, computeNodeDir(node)))
            .npmInstallCache(rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache"))
            .npmrc(rootProject.file("config/spotless/.npmrc"))
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
            .nodeExecutable(computeNodeExec(node, computeNodeDir(node)))
            .npmInstallCache(rootProject.layout.projectDirectory.dir(".gradle/spotless-npm-install-cache"))
            .npmrc(rootProject.file("config/spotless/.npmrc"))
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

listOf("spotlessJson5", "spotlessProperties", "spotlessToml").forEach { name ->
    tasks.named(name) {
        dependsOn(rootProject.tasks.nodeSetup)
    }
}

tasks.spotlessCheck {
    if (!ci) {
        dependsOn(tasks.spotlessApply)
    }
}
