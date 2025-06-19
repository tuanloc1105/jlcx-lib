# LCXDataSource Unit Tests

This directory contains comprehensive unit tests for the `LCXDataSource` class.

## Test Structure

### 1. LCXDataSourceTest.java
Basic unit tests covering:
- Initialization with valid/invalid parameters
- Connection retrieval from pool
- Connection validation and recreation
- Database version retrieval
- Pool statistics (total, active, idle connections)
- Error handling scenarios

### 2. LCXDataSourceIntegrationTest.java
Integration tests covering:
- Concurrent connection requests
- Connection pool exhaustion scenarios
- Connection pool expansion
- Connection validation with different states
- Database type compatibility
- Shutdown hook behavior

### 3. LCXDataSourcePerformanceTest.java
Performance and stress tests covering:
- High concurrency scenarios
- Connection pool under load
- Memory usage monitoring
- Response time measurements
- Scalability testing
- Long-running stress tests

## Running the Tests

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- JUnit 5
- Mockito

### Running All Tests
```bash
mvn test -Dtest="*LCXDataSource*"
```

### Running Specific Test Classes
```bash
# Basic unit tests
mvn test -Dtest="LCXDataSourceTest"

# Integration tests
mvn test -Dtest="LCXDataSourceIntegrationTest"

# Performance tests
mvn test -Dtest="LCXDataSourcePerformanceTest"
```

### Running Specific Test Methods
```bash
# Run a specific test method
mvn test -Dtest="LCXDataSourceTest#testInit_WithValidParameters_ShouldCreateDataSource"

# Run multiple specific test methods
mvn test -Dtest="LCXDataSourceTest#testGet_WithIdleConnection_ShouldReturnConnection,testGet_WithInvalidConnection_ShouldRecreateConnection"
```

## Test Configuration

The tests use mock objects to simulate database connections and avoid requiring actual database instances. The configuration is defined in:

- `test-config.properties` - Test configuration properties
- Mock setup in `@BeforeEach` methods

## Test Coverage

### Core Functionality
- ✅ Connection pool initialization
- ✅ Connection retrieval and management
- ✅ Connection validation and recreation
- ✅ Pool statistics
- ✅ Database version retrieval
- ✅ Error handling

### Edge Cases
- ✅ Invalid connection parameters
- ✅ Pool exhaustion scenarios
- ✅ Connection timeouts
- ✅ Invalid connection entries
- ✅ Database connection failures

### Performance
- ✅ High concurrency scenarios
- ✅ Memory usage monitoring
- ✅ Response time measurements
- ✅ Scalability testing
- ✅ Stress testing

### Integration
- ✅ Multi-threaded access
- ✅ Different database types
- ✅ Shutdown behavior
- ✅ Resource cleanup

## Test Results

When running the tests, you'll see output like:

```
Performance Test Results:
Total requests: 1000
Successful requests: 998
Failed requests: 2
Total time: 1500ms
Throughput: 665.33 requests/second

Load Test Results:
Total requests: 1000
Successful requests: 950
Timeout requests: 50
Total time: 3000ms

Response Time Test Results:
Iterations: 1000
Average time: 2.45ms
Minimum time: 1.23ms
Maximum time: 15.67ms
```

## Troubleshooting

### Common Issues

1. **Test Timeouts**
   - Increase timeout values in performance tests
   - Check system resources

2. **Mock Setup Issues**
   - Verify mock behavior in `@BeforeEach` methods
   - Check static method mocking setup

3. **Memory Issues**
   - Reduce pool sizes in performance tests
   - Increase JVM heap size if needed

### Debug Mode
Run tests with debug output:
```bash
mvn test -Dtest="*LCXDataSource*" -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

## Contributing

When adding new tests:

1. Follow the existing naming convention: `test[MethodName]_[Scenario]_[ExpectedResult]`
2. Use descriptive test names
3. Include proper Arrange-Act-Assert structure
4. Add appropriate assertions
5. Document complex test scenarios
6. Update this README if adding new test categories

## Dependencies

The tests require the following dependencies (already included in pom.xml):
- JUnit Jupiter (JUnit 5)
- Mockito
- SLF4J (for logging)
- Apache Commons Lang3 
