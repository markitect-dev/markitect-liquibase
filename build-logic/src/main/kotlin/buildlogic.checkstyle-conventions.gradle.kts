plugins {
    checkstyle
}

configurations.checkstyle {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.google.collections:google-collections"))
            .using(module(libs.com.google.guava.guava.get().toString()))
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}
