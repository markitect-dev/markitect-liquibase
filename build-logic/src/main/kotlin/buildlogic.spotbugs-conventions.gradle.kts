import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("com.github.spotbugs")
}

dependencies {
    spotbugsPlugins(libs.com.h3xstream.findsecbugs.findsecbugs.plugin)
}

spotbugs {
    toolVersion = libs.versions.spotbugs.asProvider().get()
}

tasks.withType<SpotBugsTask>().configureEach {
    reportLevel = Confidence.LOW
    effort = Effort.MAX
    excludeFilter = rootProject.file("config/spotbugs/exclude.xml")
    reports {
        reports.create("html") {
            enabled = true
        }
        reports.create("xml") {
            enabled = true
        }
    }
}

tasks.register("spotbugsAll") {
    group = "verification"
    dependsOn(tasks.withType<SpotBugsTask>())
}
