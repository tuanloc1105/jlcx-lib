# LCX Library - Gradle Build

This project is being converted from Maven to Gradle. The `pom.xml` files are still kept for reference.

## Project Structure

```
jlcx-lib/
├── build.gradle                    # Root build file
├── settings.gradle                 # Project settings
├── gradle.properties               # Gradle properties
├── gradle/wrapper/                 # Gradle wrapper
├── common-lib/                     # Common library module
│   └── build.gradle
├── processor/                      # Processor module
│   └── build.gradle
├── grpc-proto-plugin/              # gRPC plugin module
│   └── build.gradle
└── example/                        # Example module (commented out)
    └── build.gradle
```

## Basic Gradle Commands

### Build entire project
```bash
./gradlew build
```

### Clean and build
```bash
./gradlew clean build
```

### Build individual modules
```bash
./gradlew :common-lib:build
./gradlew :processor:build
./gradlew :grpc-proto-plugin:build
```

### Run tests
```bash
./gradlew test
```

### Publish to Maven local
```bash
./gradlew publishToMavenLocal
```

### Create fat jar for example module
```bash
./gradlew :example:fatJar
```

## Migration from Maven

### Dependencies
- All dependencies from `pom.xml` have been converted to `build.gradle`
- Versions are defined in the `ext` block
- Scopes have been converted:
  - `compile` → `implementation`
  - `provided` → `compileOnly`
  - `test` → `testImplementation`
  - `runtime` → `runtimeOnly`

### Plugins
- `maven-compiler-plugin` → `java` plugin
- `maven-assembly-plugin` → custom `fatJar` task
- `maven-surefire-plugin` → `test` task with JUnit 5
- `maven-dependency-plugin` → not needed in Gradle

### Build lifecycle
- `mvn clean` → `./gradlew clean`
- `mvn compile` → `./gradlew compileJava`
- `mvn test` → `./gradlew test`
- `mvn package` → `./gradlew jar`
- `mvn install` → `./gradlew publishToMavenLocal`

## Special Configuration

### common-lib module
- Removed Lombok dependency (as in pom.xml)
- Disabled assembly plugin

### processor module
- Dependency on common-lib
- Lombok with provided scope

### grpc-proto-plugin module
- Maven plugin configuration
- Dependencies with provided scope

### example module
- Fat jar task to create jar with dependencies
- Main class configuration

## Notes

1. All `pom.xml` files are still kept for reference
2. The `example` module is commented out in `settings.gradle` as in `pom.xml`
3. All versions and dependencies remain the same as in Maven
4. Gradle wrapper uses version 8.5

## Troubleshooting

If you encounter build errors:
1. Run `./gradlew clean`
2. Delete the `.gradle` directory if it exists
3. Run `./gradlew build` again

## Key Features

- **Multi-module project**: Supports building individual modules or the entire project
- **Dependency management**: Centralized version management in the root build file
- **Testing**: JUnit 5 support with proper test configuration
- **Publishing**: Maven publishing support for all modules
- **Fat JARs**: Custom tasks for creating executable JARs with dependencies
- **IDE support**: Compatible with IntelliJ IDEA, Eclipse, and VS Code

## Development Workflow

1. **Setup**: Clone the repository and ensure Java 11+ is installed
2. **Build**: Run `./gradlew build` to build all modules
3. **Test**: Run `./gradlew test` to execute all tests
4. **Develop**: Use your preferred IDE with Gradle support
5. **Package**: Use `./gradlew jar` for regular JARs or `./gradlew fatJar` for executable JARs
