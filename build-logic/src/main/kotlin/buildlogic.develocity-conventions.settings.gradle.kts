plugins {
    id("com.gradle.develocity")
}

val ci = providers.environmentVariable("CI").isPresent

develocity {
    buildScan {
        if (ci) {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
        }
        publishing.onlyIf { false }
    }
}
