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

## Code Quality

The project use Spotless for code formatting.

### Running Spotless

Spotless is used to automatically format code according to the project's style guidelines.

To automatically apply formatting fixes:

```bash
./gradlew spotlessApply
```

**Note**: Always run `spotlessApply` before committing your changes to ensure consistent code formatting across the project.

## Questions?

If you have questions or need help, please open an issue on GitHub.
