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
   ./gradlew --write-verification-metadata sha256 dependencies --write-locks
   ```

2. Clean up any temporary files that were generated:
   ```bash
   rm -f gradle/verification-metadata.dryrun.xml
   rm -f settings-gradle.lockfile
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

## Questions?

If you have questions or need help, please open an issue on GitHub.
