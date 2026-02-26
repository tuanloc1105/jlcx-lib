# Utilities & Common Infrastructure

## Overview

The `vn.com.lcx.common` package provides 28+ utility classes, a package scanner, global
constants, and custom exceptions. All utility classes are `final` with private constructors
(static-only usage).

---

## Package Scanner

### PackageScanner

Runtime class discovery for the DI container.

**Location:** `vn.com.lcx.common.scanner.PackageScanner`

```java
List<Class<?>> classes = PackageScanner.findClasses("com.example.app");
```

| Method | Description |
|--------|-------------|
| `static List<Class<?>> findClasses(String packageName)` | Scans package for all `.class` files |

- Supports both filesystem directories (`file://`) and JAR files (`jar:`)
- Uses `Thread.currentThread().getContextClassLoader()`
- Silently ignores classes that fail to load (`ClassNotFoundException`)
- Returns empty list on I/O errors

---

## Utility Classes

### BCryptUtils - Password Hashing

```java
String hash = BCryptUtils.hashPassword("myPassword");
BCryptUtils.comparePassword("myPassword", hash); // throws if mismatch
```

| Method | Description |
|--------|-------------|
| `static String hashPassword(String password)` | Hash with BCrypt (4 rounds) |
| `static void comparePassword(String password, String passHash)` | Verify password; throws `IllegalArgumentException` on mismatch |

---

### RSAUtils - RSA Encryption

```java
RSAPublicKey pubKey = RSAUtils.getPublicKey(pemString);
String encrypted = RSAUtils.encrypt("secret data", pubKey);
String decrypted = RSAUtils.decrypt(encrypted, privateKey);
```

| Method | Description |
|--------|-------------|
| `static String readKeyFromResource(String path, boolean isPrivate)` | Read PEM key from classpath |
| `static String readKey(String path, boolean isPrivate)` | Read PEM key from filesystem |
| `static RSAPublicKey getPublicKey(String pem)` | Parse PEM to RSAPublicKey |
| `static RSAPrivateKey getPrivateKey(String pem)` | Parse PEM to RSAPrivateKey |
| `static String encrypt(String data, RSAPublicKey key)` | Encrypt → Base64 string |
| `static String decrypt(String data, RSAPrivateKey key)` | Base64 → decrypt → String |

Algorithm: `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`

---

### AESUtils - AES Encryption

Provides AES encryption/decryption with ECB and CBC modes.

---

### FileUtils - File Operations

Comprehensive file I/O with cross-platform support.

| Method | Description |
|--------|-------------|
| `static boolean writeContentToFile(String path, String content)` | Overwrite file |
| `static boolean appendContentToFile(String path, String content)` | Append to file |
| `static String read(String path)` | Read entire file as string |
| `static List<String> readToList(String path)` | Read non-empty trimmed lines |
| `static String pathJoining(String... parts)` | Join with `File.separator` |
| `static String pathJoiningWithSlash(String... parts)` | Join with `/` |
| `static boolean createFolderIfNotExists(String path)` | Create directory tree |
| `static void deleteFolder(File folder)` | Recursive delete |
| `static String encodeFileToBase64(String path)` | File → Base64 string |
| `static byte[] readFileIntoBytes(String path)` | File → byte array |
| `static String getFileName(String path)` | Extract filename |
| `static String getFileExtension(String path)` | Extract extension |
| `static boolean checkIfExist(String path)` | Existence check |
| `static boolean isReadableTextFile(String path)` | Check text MIME type |
| `static boolean createFile(String path)` | Create empty file |
| `static boolean createDirectory(String path)` | Create directory |
| `static boolean delete(String path)` | Recursive delete |
| `static boolean copy(String source, String dest)` | Recursive copy |
| `static boolean move(String source, String dest)` | Move with replace |
| `static boolean rename(String source, String newName)` | Rename in-place |
| `static List<String> listFiles(String dir)` | List child names |
| `static boolean changeFilePermission(String path, SystemUserPermission owner, group, other)` | POSIX/Windows permissions |
| `static boolean changeDirectoryPermissionsRecursively(...)` | Recursive permission change |
| `static String readResourceFileAsText(ClassLoader cl, String name)` | Read classpath resource |

**SystemUserPermission** inner class: `readable`, `writeable`, `executable` boolean fields.
`handlePermission()` returns numeric value (read=4, write=2, execute=1).

---

### ObjectUtils - Reflection & Object Operations

| Method | Description |
|--------|-------------|
| `static <S,T> T mapObjects(S source, Class<T> target)` | Copy matching fields to new instance |
| `static boolean isNullOrEmpty(Object obj)` | Null, empty collection, or blank string |
| `static Class<?> wrapPrimitive(Class<?> type)` | `int` → `Integer`, etc. |
| `static Object getDefaultValue(Class<?> type)` | Default for primitive types |
| `static List<Class<?>> getExtendAndInterfaceClasses(Class<?> target)` | Superclass + interfaces |
| `static List<Type> getTypeParameters(Class<?> clazz)` | Generic type parameters |

---

### MyStringUtils - String Manipulation

Comprehensive string utilities including:
- JSON string parsing
- URL encoding/decoding
- Case conversion (camelCase, PascalCase, CONSTANT_CASE)
- Vietnamese diacritical mark handling
- Text formatting and truncation

---

### MyCollectionUtils - Collection Operations

| Method | Description |
|--------|-------------|
| List splitting into fixed-size batches | |
| Null element removal | |

---

### WordCaseUtils - Case Conversion

| Method | Description |
|--------|-------------|
| camelCase conversion | `myFieldName` |
| PascalCase conversion | `MyFieldName` |
| CONSTANT_CASE conversion | `MY_FIELD_NAME` |

---

### DateTimeUtils - Date/Time Utilities

Timezone-aware date/time operations supporting 28+ timezones including:
VST (Vietnam), JST (Japan), EST (US Eastern), PST (US Pacific), UTC, and more.

---

### ExceptionUtils - Stack Trace Extraction

```java
String stackTrace = ExceptionUtils.getStackTrace(exception);
```

| Method | Description |
|--------|-------------|
| `static String getStackTrace(Throwable t)` | Full stack trace as string |

---

### SerializeUtils - Java Serialization

```java
SerializeUtils.serialize(myObject, "/data", "cache");  // → /data/cache.ser
MyObject obj = SerializeUtils.deserialize("/data", "cache");
```

| Method | Description |
|--------|-------------|
| `static <T extends Serializable> void serialize(T obj, String path, String name)` | Object → `.ser` file |
| `static <T extends Serializable> T deserialize(String path, String name)` | `.ser` file → Object |

---

### YamlProperties - YAML Configuration

```java
YamlProperties props = new YamlProperties("application.yaml", classLoader);
String port = props.getProperty("server.port", "8080");
Integer poolSize = props.getProperty_("database.max-pool-size");
```

| Method | Description |
|--------|-------------|
| `String getProperty(String key)` | Dot-notation nested value lookup |
| `String getProperty(String key, String defaultValue)` | With default |
| `<T> T getProperty_(String key)` | Generic getter |

Supports nested keys: `"app.server.port"` traverses `map.get("app").get("server").get("port")`.

---

### LCXProperties - Environment-Aware Properties

Wraps `YamlProperties` with environment variable substitution:

```yaml
database:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
```

`${ENV_VAR:default}` syntax resolves environment variables with fallback defaults.

---

### HttpUtils - HTTP Client

HTTP client with JSON/XML support, connection pooling, and request masking for sensitive data.

---

### JsonMaskingUtils - Sensitive Data Masking

Masks sensitive fields in JSON strings. Protects 60+ field names including passwords, tokens,
SSN, credit card numbers, and PII fields.

---

### LogUtils - Structured Logging

SLF4J-based logging with MDC support:
- Automatic `trace_id` propagation
- `operation_name` tracking
- Vert.x `RoutingContext` integration
- Log levels: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

---

### RandomUtils - Random Generation

Random string/number generation, list shuffling, and random element selection.

---

### NumberFormatUtils - Number Formatting

`BigDecimal` formatting using US locale.

---

### UUIDv7 - UUID Generation

Generates UUID v7 (time-ordered) using timestamp and cryptographically random bytes.

---

### ThreadUtils - Thread Monitoring

Thread monitoring, safe interruption, and stack trace logging.

---

### JVMSystemInfo - JVM Monitoring

JVM memory and processor information with periodic monitoring via Vert.x timers.

---

### CommonUtils - General Utilities

Garbage collection triggers, random number generation, and application banner logging.

---

### TopoSortUtils - Topological Sort

Topological sorting implementation for dependency graph ordering.

---

### DisableSsl - SSL Bypass

Disables SSL certificate verification (for development/testing only).

---

### ShellCommandRunningUtils - Shell Execution

Executes shell commands from Java.

---

### SocketUtils - Socket Operations

Socket-level networking utilities.

---

### SimpleCipherHandler - Lightweight Cipher

Simple symmetric cipher utility.

---

### PropertiesUtils - Property Loading

```java
LCXProperties props = PropertiesUtils.loadYamlProperties("application.yaml");
LCXProperties empty = PropertiesUtils.emptyProperty();
```

---

## Constants

### CommonConstant

Global constants used throughout the framework.

**String patterns:**

| Constant | Value |
|----------|-------|
| `DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN` | `yyyy-MM-dd HH:mm:ss.SSS` |
| `DEFAULT_LOCAL_DATE_STRING_PATTERN` | `yyyy-MM-dd` |
| `DEFAULT_LOCAL_DATE_TIME_VIETNAMESE_STRING_PATTERN` | `dd-MM-yyyy HH:mm:ss.SSS` |
| `LOCAL_DATE_TIME_STRING_PATTERN_1` | `yyyy-MM-dd'T'HH:mm:ss` |
| `LOCAL_DATE_TIME_STRING_PATTERN_2` | `yyyy-MM-dd'T'HH:mm:ss.SSS` |

**Common values:**

| Constant | Value |
|----------|-------|
| `EMPTY_STRING` | `""` |
| `HYPHEN` | `"-"` |
| `NULL_STRING` | `"null"` |
| `UTF_8_STANDARD_CHARSET` | `"UTF-8"` |

**MDC keys:**

| Constant | Value |
|----------|-------|
| `TRACE_ID_MDC_KEY_NAME` | `"trace_id"` |
| `OPERATION_NAME_MDC_KEY_NAME` | `"operation_name"` |
| `CURRENT_USER` | `"current_user"` |

**Runtime values:**

| Constant | Description |
|----------|-------------|
| `ROOT_DIRECTORY_PROJECT_PATH` | Absolute path of working directory |
| `applicationConfig` | Loaded `LCXProperties` (volatile) |
| `DATA_TYPE_AND_SQL_STATEMENT_METHOD_MAP` | Java type → `SqlStatementHandler` map |
| `SENSITIVE_FIELD_NAMES` | Immutable list of 60+ sensitive field names |

**Sensitive field names include:** `password`, `pwd`, `secret`, `ssn`, `creditCardNumber`,
`cvv`, `bankAccountNumber`, `token`, `accessToken`, `refreshToken`, `apiKey`, `sessionId`,
`email`, `phone`, `address`, `ipAddress`, and many more.

### JavaSqlResultSetConstant

Maps SQL data types to `ResultSet` getter methods and default values for type-safe
result extraction.

---

## Custom Exceptions

All exceptions extend `RuntimeException`.

| Exception | Description |
|-----------|-------------|
| `CacheException` | Cache operation failures |
| `ValidationException` | Data validation errors |
| `DuplicateInstancesException` | Duplicate bean name in ClassPool |
| `LCXDataSourceException` | Data source operation errors |
| `LCXDataSourcePropertiesException` | Data source configuration errors |
| `HikariLcxDataSourceException` | HikariCP-specific errors |
| `ConnectionEntryException` | Connection pool errors |

---

## Annotation Processors

See [annotation-processors.md](annotation-processors.md) for full documentation on all 9
annotation processors (MapperClassProcessor, SQLMappingProcessor, ServiceProcessor, etc.).

---

## Key Source Files

| File | Description |
|------|-------------|
| `common/scanner/PackageScanner.java` | Runtime class discovery |
| `common/utils/BCryptUtils.java` | Password hashing |
| `common/utils/RSAUtils.java` | RSA encryption |
| `common/utils/AESUtils.java` | AES encryption |
| `common/utils/FileUtils.java` | File operations |
| `common/utils/ObjectUtils.java` | Reflection utilities |
| `common/utils/MyStringUtils.java` | String manipulation |
| `common/utils/DateTimeUtils.java` | Date/time utilities |
| `common/utils/YamlProperties.java` | YAML configuration |
| `common/utils/LCXProperties.java` | Environment-aware properties |
| `common/utils/HttpUtils.java` | HTTP client |
| `common/utils/JsonMaskingUtils.java` | Sensitive data masking |
| `common/utils/LogUtils.java` | Structured logging |
| `common/utils/ExceptionUtils.java` | Stack trace extraction |
| `common/utils/SerializeUtils.java` | Java serialization |
| `common/utils/UUIDv7.java` | UUID v7 generation |
| `common/utils/RandomUtils.java` | Random generation |
| `common/utils/WordCaseUtils.java` | Case conversion |
| `common/utils/CommonUtils.java` | General utilities |
| `common/constant/CommonConstant.java` | Global constants |
| `common/exception/*.java` | Custom exceptions |
| `processor/SQLMappingProcessor.java` | Entity code generator |
| `processor/MapperClassProcessor.java` | Mapper code generator |
| `processor/ServiceProcessor.java` | Service proxy generator |
