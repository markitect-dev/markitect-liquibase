---
name: checkstyle
description: >-
  Run Checkstyle for the markitect-liquibase Gradle build and reliably surface
  every violation even when the Checkstyle tasks are UP-TO-DATE, then fix them.
  Use when asked to get, review, or fix Checkstyle warnings/violations, or when
  MissingJavadocType / MissingJavadocMethod findings need to be addressed.
---

# Checkstyle workflow

This is a multi-module Gradle (Kotlin DSL) build. The `checkstyle` convention
lives in `build-logic/src/main/kotlin/buildlogic.checkstyle-conventions.gradle.kts`
and registers an aggregate `checkstyle` task that depends on every
`Checkstyle`-type task (`checkstyleMain` / `checkstyleTest`) across all modules.
The shared config is `config/checkstyle/checkstyle.xml` (Google-based).

## 1. Get the warnings (even when tasks are UP-TO-DATE)

Gradle's incremental build skips `checkstyleMain` / `checkstyleTest` when their
inputs and outputs are unchanged, so a plain re-run prints **nothing**. Force
execution instead of running `clean` (which needlessly discards other outputs):

```
./gradlew checkstyle --rerun-tasks --console=plain          # all modules
./gradlew checkstyleMain --rerun --console=plain            # single task
```

- `--rerun-tasks` ignores up-to-date checks for the whole task graph.
- `--rerun` (Gradle 7.5+) scopes the force to one named task.
- On Windows use `.\gradlew.bat`.

## 2. Read the reports without re-running

Each module writes a report you can read directly:

```
<module>/build/reports/checkstyle/main.html   # human-readable
<module>/build/reports/checkstyle/main.xml    # machine-readable
<module>/build/reports/checkstyle/test.xml
```

Aggregate every XML report into one sorted list (PowerShell):

```powershell
Get-ChildItem -Recurse -Filter *.xml |
  Where-Object { $_.FullName -match 'reports\\checkstyle' } |
  ForEach-Object { [xml]$x = Get-Content $_.FullName
    foreach ($f in $x.checkstyle.file) { foreach ($e in $f.error) {
      [PSCustomObject]@{ File=$f.name; Line=[int]$e.line
        Rule=($e.source -replace '.*\.',''); Msg=$e.message } } } } |
  Sort-Object File,Line | Format-Table -Auto
```

## 3. Understand severity

Missing-Javadoc checks are emitted at `info` severity by default (see
`checkstyle.missingjavadoc.severity` in `config/checkstyle/checkstyle.xml`), so
they do **not** fail the build even though `maxWarnings = 0` — that limit counts
`warning`-severity items, not `info`. "Address the warnings" therefore means
adding the missing Javadoc, not tuning the threshold.

Scope of the two most common findings:
- `MissingJavadocType`: public/protected types (`CLASS_DEF`, `INTERFACE_DEF`,
  `ENUM_DEF`, `RECORD_DEF`, `ANNOTATION_DEF`).
- `MissingJavadocMethod`: public/protected methods & constructors.
  `allowMissingPropertyJavadoc = true`, and `@Override` / `@Test` methods are
  exempt, so simple getters/setters and overrides usually need nothing.

## 4. Fix

Add a Javadoc comment immediately above the flagged type/method. It must sit
**above any annotations** — placing it between an annotation and the declaration
triggers `InvalidJavadocPosition`, a `warning` that DOES fail the build — and
below the Apache license header. Keep it short and factual: the first sentence
must start with a capital letter and end with a period (`SummaryJavadoc`);
`@param`/`@return` tags are not required. Match house style (2-space indent, no
author tags). Watch out for multi-line annotations with text blocks. Example:

```java
/** Builds configured Liquibase database connections. */
public final class DatabaseConnectionBuilder {
  /** Creates a new database connection builder. */
  public static DatabaseConnectionBuilder newBuilder() { ... }
```

For an annotated declaration the Javadoc goes first, then the annotations:

```java
/** Creates a database using a Liquibase change. */
@DatabaseChange(name = "createDatabase", ...)
@SuppressWarnings("squid:S2160")
public class CreateDatabaseChange extends AbstractChange {
```

Do not add Javadoc to `@Override` methods or private members — that only adds
noise the check does not require.

## 5. Verify

Re-run forcing execution and confirm zero violations:

```
./gradlew checkstyle --rerun-tasks --console=plain
```

A clean run reports `BUILD SUCCESSFUL` with no `[ant:checkstyle]` lines and no
`Checkstyle files with violations` summaries.
