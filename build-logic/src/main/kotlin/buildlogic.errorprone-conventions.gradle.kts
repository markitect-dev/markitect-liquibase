import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

dependencies {
    errorprone(libs.com.google.errorprone.error.prone.core)
    errorprone(libs.com.uber.nullaway.nullaway)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        allDisabledChecksAsWarnings = true
        disable("AndroidJdkLibsChecker")
        disable("BooleanParameter")
        disable("BuilderReturnThis")
        disable("CanIgnoreReturnValueSuggester")
        disable("Java7ApiChecker")
        disable("Java8ApiChecker")
        disable("Var")
        errorproneArgs.add("-XepAllSuggestionsAsWarnings")
        nullaway {
            excludedFieldAnnotations.add("org.mockito.Captor")
            excludedFieldAnnotations.add("org.mockito.Mock")
            warn()
        }
    }
}

nullaway {
    annotatedPackages.add("dev.markitect.liquibase")
}
