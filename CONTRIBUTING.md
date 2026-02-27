# Contributing to Markitect Liquibase

Thank you for your interest in contributing to Markitect Liquibase Extensions & Integrations!

## Development Setup

### Prerequisites

- Java 25 (Azul Zulu recommended)
- Gradle (wrapper included)
- Git

### Building the Project

```bash
./gradlew build
```

## Maintenance Tasks

### Updating Gradle Verification Metadata

When dependencies are updated (e.g., by Renovate or manual dependency changes), the Gradle verification metadata file needs to be regenerated to include checksums for the new dependency versions.

#### Prerequisites

- Ensure you have all Git remote branches fetched:
  ```bash
  git remote set-branches origin '*'
  git fetch origin
  ```

#### Steps to Update

1. Run the Gradle command to regenerate verification metadata:
   ```bash
   ./gradlew --write-verification-metadata sha256 spotlessApply
   ```

2. Clean up any temporary files that were generated:
   ```bash
   rm -f gradle/verification-metadata.dryrun.xml
   ```

3. Review the changes to `gradle/verification-metadata.xml`:
   ```bash
   git diff gradle/verification-metadata.xml
   ```

4. Verify the build works with the updated metadata:
   ```bash
   ./gradlew help
   ```

5. Commit the changes:
   ```bash
   git add gradle/verification-metadata.xml
   git commit -m "Update Gradle verification metadata"
   ```

#### What Gets Updated

The verification metadata file (`gradle/verification-metadata.xml`) contains SHA-256 checksums for all dependencies used in the build. This ensures the integrity and security of downloaded dependencies.

When running the update command, Gradle will:
- Resolve all project dependencies
- Generate SHA-256 checksums for each artifact
- Update the XML file with the new checksums

### Pruning Gradle Verification Metadata

Over time, the verification metadata file accumulates checksums for old dependency versions that are no longer used. Pruning removes these unused entries while keeping only the current dependencies.

#### When to Prune

- After multiple dependency updates
- When the file has grown significantly
- As part of regular maintenance (e.g., quarterly)

#### Steps to Prune

1. Delete the contents of the `<components>` section while keeping the trusted artifacts configuration:
   ```bash
   cat > gradle/verification-metadata.xml << 'EOF'
   <?xml version="1.0" encoding="UTF-8"?>
   <verification-metadata xmlns="https://schema.gradle.org/dependency-verification" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="https://schema.gradle.org/dependency-verification https://schema.gradle.org/dependency-verification/dependency-verification-1.3.xsd">
      <configuration>
         <verify-metadata>false</verify-metadata>
         <verify-signatures>false</verify-signatures>
         <trusted-artifacts>
            <trust group="org.gradle" name="github-dependency-graph-gradle-plugin"/>
            <trust file=".*-javadoc[.]jar" regex="true"/>
            <trust file=".*-sources[.]jar" regex="true"/>
            <trust file=".*-src[.]zip" regex="true"/>
         </trusted-artifacts>
      </configuration>
      <components>
      </components>
   </verification-metadata>
   EOF
   ```

   This creates a minimal file with an empty `<components>` section that will be repopulated in the next step.

2. Regenerate verification metadata for current dependencies:
   ```bash
   ./gradlew --write-verification-metadata sha256 spotlessApply
   ```

3. Verify the build works with the pruned metadata:
   ```bash
   ./gradlew help
   ```

4. Review the changes (should show old versions removed):
   ```bash
   git diff --stat gradle/verification-metadata.xml
   ```

5. Commit the changes:
   ```bash
   git add gradle/verification-metadata.xml
   git commit -m "Prune Gradle verification metadata"
   ```

**Note**: The `<components>` section is required by the schema but you can delete its contents (all `<component>` entries). The regeneration process will repopulate it with only the current dependencies needed by the project.

#### Troubleshooting

**Issue: `No such reference 'origin/main'` error**

Solution: Make sure you have fetched all remote branches:
```bash
git remote set-branches origin '*'
git fetch origin
```

**Issue: Java version mismatch**

The project requires Java 25. If you encounter Java toolchain errors, ensure you have Java 25 installed. Note that some tasks (like `dependencies`) can be run without full compilation.

**Issue: Build failures during verification metadata update**

If you encounter build failures when running `./gradlew check` or `./gradlew build`, you can use simpler tasks like `./gradlew dependencies` or `./gradlew help` to update the metadata without requiring a full successful build.

## Code Quality

The project uses:
- Spotless for code formatting
- Gradle's dependency verification for security

### Running Spotless

Spotless is used to automatically format code according to the project's style guidelines.

To check if your code is properly formatted:
```bash
./gradlew spotlessCheck
```

To automatically apply formatting fixes:
```bash
./gradlew spotlessApply
```

**Note**: Always run `spotlessApply` before committing your changes to ensure consistent code formatting across the project.

## Questions?

If you have questions or need help, please open an issue on GitHub.
