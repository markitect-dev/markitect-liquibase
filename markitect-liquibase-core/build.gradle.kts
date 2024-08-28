plugins {
    id("buildlogic.java-conventions")
    id("buildlogic.publishing-conventions")
}

dependencies {
    api(project(":markitect-liquibase-base"))
    api(project(":markitect-liquibase-logging"))
    api(libs.org.liquibase.liquibase.core)
    compileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    compileOnly(libs.com.google.errorprone.error.prone.annotations)
    compileOnly(libs.org.checkerframework.checker.qual)

    testCompileOnly(libs.com.github.spotbugs.spotbugs.annotations)
    testCompileOnly(libs.com.google.errorprone.error.prone.annotations)
    testCompileOnly(libs.org.checkerframework.checker.qual)
    testImplementation(libs.com.google.guava.guava)
    testImplementation(libs.org.junit.pioneer.junit.pioneer)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.jdbc)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.org.testcontainers.junit.jupiter)
    testImplementation(libs.org.testcontainers.mssqlserver)
    testImplementation(libs.org.testcontainers.postgresql)
    testRuntimeOnly(libs.com.fasterxml.jackson.core.jackson.databind)
    testRuntimeOnly(libs.com.h2database.h2)
    testRuntimeOnly(libs.com.microsoft.sqlserver.mssql.jdbc)
    testRuntimeOnly(libs.org.hsqldb.hsqldb)
    testRuntimeOnly(libs.org.junit.pioneer.junit.pioneer) {
        capabilities {
            requireCapability("org.junit-pioneer:junit-pioneer-jackson")
        }
    }
    testRuntimeOnly(libs.org.postgresql.postgresql)
}

sonar {
    properties {
        property(
            "sonar.cpd.exclusions",
            listOf(
                "src/main/java/dev/markitect/liquibase/change/CreateSchemaChange.java",
                "src/main/java/dev/markitect/liquibase/change/DropSchemaChange.java",
                "src/main/java/dev/markitect/liquibase/change/MarkitectInsertDataChangeMssql.java",
                "src/main/java/dev/markitect/liquibase/change/MarkitectLoadDataChangeMssql.java",
                "src/main/java/dev/markitect/liquibase/change/MarkitectLoadUpdateDataChangeMssql.java",
                "src/main/java/dev/markitect/liquibase/database/h2/MarkitectH2Database.java",
                "src/main/java/dev/markitect/liquibase/database/hsqldb/MarkitectHsqlDatabase.java",
                "src/main/java/dev/markitect/liquibase/database/mssql/MarkitectMssqlDatabase.java",
                "src/main/java/dev/markitect/liquibase/database/postgresql/MarkitectPostgresDatabase.java",
                "src/main/java/dev/markitect/liquibase/precondition/CatalogExistsPrecondition.java",
                "src/main/java/dev/markitect/liquibase/precondition/SchemaExistsPrecondition.java",
                "src/main/java/dev/markitect/liquibase/sqlgenerator/MarkitectInsertOrUpdateGeneratorHsql.java",
                "src/main/java/dev/markitect/liquibase/sqlgenerator/MarkitectInsertOrUpdateGeneratorPostgres.java",
                "src/main/java/dev/markitect/liquibase/statement/CreateSchemaStatement.java",
                "src/main/java/dev/markitect/liquibase/statement/DropSchemaStatement.java",
                "src/main/java/dev/markitect/liquibase/statement/SchemaExistsStatement.java",
            ).joinToString(","),
        )
    }
}

description = "Markitect Liquibase Core"

project.extra["automaticModuleName"] = "dev.markitect.liquibase.core"
