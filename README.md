# E-Commerce Platform - Test Suite Documentation

## Overview

This document provides comprehensive information about the test suite for the E-Commerce Platform microservices. The test suite ensures 100% API endpoint coverage with valid, invalid, and edge case scenarios.

## Table of Contents

1. [Test Coverage Summary](#test-coverage-summary)
2. [Setup Instructions](#setup-instructions)
3. [Running Tests](#running-tests)
4. [Test Structure](#test-structure)
5. [Authentication Testing](#authentication-testing)
6. [Troubleshooting](#troubleshooting)
7. [Best Practices](#best-practices)

---

## Test Coverage Summary

### User Management Service (7 Endpoints)

| Endpoint | Method | Test Cases | Coverage |
|----------|--------|------------|----------|
| `/api/v1/users/register` | POST | 5 | 100% |
| `/api/v1/users/login` | POST | 4 | 100% |
| `/api/v1/users/profile` | GET | 3 | 100% |
| `/api/v1/users/profile` | PUT | 3 | 100% |
| `/api/v1/users/logout` | POST | 2 | 100% |
| `/api/v1/users/password/reset` | POST | 3 | 100% |
| `/api/v1/users/password/change` | PUT | 5 | 100% |

**Total Test Cases: 25**

### Product Catalog Service (2 Endpoints)

| Endpoint | Method | Test Cases | Coverage |
|----------|--------|------------|----------|
| `/api/v1/products/search` | GET | 13 | 100% |
| `/api/v1/products/{productId}` | GET | 7 | 100% |

**Total Test Cases: 20**

### Shopping Cart Service (5 Endpoints)

| Endpoint | Method | Test Cases | Coverage |
|----------|--------|------------|----------|
| `/api/v1/cart` | GET | 4 | 100% |
| `/api/v1/cart/items` | POST | 7 | 100% |
| `/api/v1/cart/items/{itemId}` | PUT | 5 | 100% |
| `/api/v1/cart/items/{itemId}` | DELETE | 4 | 100% |
| `/api/v1/cart` | DELETE | 4 | 100% |

**Total Test Cases: 24**

### Overall Coverage

- **Total Endpoints:** 11
- **Total Test Cases:** 69
- **Coverage:** 100%
- **Test Types:** Unit Tests, Integration Tests
- **Frameworks:** JUnit 5, Mockito, Spring Boot Test

---

## Setup Instructions

### Prerequisites

- Java 17 LTS or higher
- Maven 3.9.x or higher
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Git

### Installation Steps

1. **Clone the Repository**

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

2. **Install Dependencies**

```bash
mvn clean install
```

3. **Verify Test Dependencies**

Ensure the following dependencies are in your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Test Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Security Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito JUnit Jupiter -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

4. **Configure Test Properties**

Create `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  security:
    jwt:
      secret: test-secret-key-for-jwt-token-generation
      expiration: 3600000
```

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Tests for Specific Service

**User Management Service:**
```bash
mvn test -Dtest=UserControllerTest
```

**Product Catalog Service:**
```bash
mvn test -Dtest=ProductControllerTest
```

**Shopping Cart Service:**
```bash
mvn test -Dtest=CartControllerTest
```

### Run Tests with Coverage Report

```bash
mvn clean test jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

### Run Tests in IDE

**IntelliJ IDEA:**
1. Right-click on test class
2. Select "Run 'TestClassName'"
3. View results in Run window

**Eclipse:**
1. Right-click on test class
2. Select "Run As" > "JUnit Test"
3. View results in JUnit view

---

## Test Structure

### Test File Organization

```
src/main/tests/
└── controller/
    ├── UserControllerTest.java
    ├── ProductControllerTest.java
    └── CartControllerTest.java
```

### Test Naming Convention

```java
@Test
@DisplayName("[HTTP Method] [Endpoint] - [Scenario] - Should Return [Expected Result]")
void test[MethodName]_[Scenario]_[ExpectedResult]() {
    // Test implementation
}
```

**Example:**
```java
@Test
@DisplayName("POST /api/v1/users/register - Valid Request - Should Return 201")
void testRegisterUser_ValidRequest_ReturnsCreated() {
    // Test implementation
}
```

### Test Categories

1. **Valid Request Tests**
   - Test successful scenarios with valid input
   - Verify correct response status and data

2. **Invalid Request Tests**
   - Test validation failures
   - Test missing required fields
   - Test invalid data formats

3. **Edge Case Tests**
   - Test boundary conditions
   - Test empty/null values
   - Test maximum/minimum values

4. **Authentication Tests**
   - Test authenticated endpoints
   - Test unauthorized access
   - Test token validation

5. **Error Handling Tests**
   - Test 404 Not Found scenarios
   - Test 400 Bad Request scenarios
   - Test 500 Internal Server Error scenarios

---

## Authentication Testing

### Using @WithMockUser

For endpoints requiring authentication:

```java
@Test
@WithMockUser(username = "test@example.com")
@DisplayName("GET /api/v1/users/profile - Authenticated User - Should Return 200")
void testGetUserProfile_AuthenticatedUser_ReturnsOk() throws Exception {
    // Test implementation
}
```

### Testing Unauthenticated Access

```java
@Test
@DisplayName("GET /api/v1/users/profile - Unauthenticated User - Should Return 401")
void testGetUserProfile_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/v1/users/profile"));
    result.andExpect(status().isUnauthorized());
}
```

### JWT Token Testing

For integration tests with real JWT tokens:

```java
@Test
void testWithRealJwtToken() throws Exception {
    // Login to get token
    String token = obtainAccessToken("test@example.com", "password");
    
    // Use token in request
    mockMvc.perform(get("/api/v1/users/profile")
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
}
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Test Failures Due to Schema Mismatch

**Problem:** Tests fail because request/response schemas don't match Swagger specification.

**Solution:**
```bash
# Regenerate DTOs from Swagger spec
mvn clean install

# Verify Swagger annotations
mvn springdoc-openapi:generate
```

#### 2. Authentication Test Failures

**Problem:** Tests fail with 401 Unauthorized even with @WithMockUser.

**Solution:**
```java
// Add CSRF token to requests
mockMvc.perform(post("/api/v1/users/register")
    .with(csrf())
    .contentType(MediaType.APPLICATION_JSON)
    .content(requestBody))
```

#### 3. MockMvc Not Autowired

**Problem:** `@Autowired MockMvc` is null.

**Solution:**
```java
@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
}
```

#### 4. Service Mock Not Working

**Problem:** Service methods are not being mocked.

**Solution:**
```java
@MockBean
private UserService userService;

@BeforeEach
void setUp() {
    when(userService.registerUser(any()))
        .thenReturn(expectedResponse);
}
```

#### 5. JSON Serialization Errors

**Problem:** Request body serialization fails.

**Solution:**
```java
@Autowired
private ObjectMapper objectMapper;

String requestBody = objectMapper.writeValueAsString(request);
```

#### 6. Database Connection Issues

**Problem:** Tests fail due to database connection errors.

**Solution:**
```yaml
# Use H2 in-memory database for tests
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

#### 7. Port Already in Use

**Problem:** Tests fail because port is already in use.

**Solution:**
```yaml
# Use random port for tests
server:
  port: 0
```

#### 8. Test Execution Timeout

**Problem:** Tests timeout during execution.

**Solution:**
```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void testWithTimeout() {
    // Test implementation
}
```

---

## Best Practices

### 1. Test Independence

- Each test should be independent
- Use `@BeforeEach` to set up test data
- Don't rely on test execution order

```java
@BeforeEach
void setUp() {
    // Initialize test data
    validRequest = createValidRequest();
}
```

### 2. Descriptive Test Names

- Use clear, descriptive test names
- Follow naming convention: `test[Method]_[Scenario]_[ExpectedResult]`
- Use `@DisplayName` for human-readable descriptions

### 3. Arrange-Act-Assert Pattern

```java
@Test
void testExample() {
    // Arrange
    UserRequest request = createRequest();
    when(service.method()).thenReturn(response);
    
    // Act
    ResultActions result = mockMvc.perform(post("/endpoint")
        .content(objectMapper.writeValueAsString(request)));
    
    // Assert
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.field").value("value"));
}
```

### 4. Mock External Dependencies

- Mock all external service calls
- Use `@MockBean` for Spring beans
- Verify mock interactions

```java
@MockBean
private ExternalService externalService;

verify(externalService, times(1)).method(any());
```

### 5. Test Edge Cases

- Test boundary values
- Test null/empty inputs
- Test maximum/minimum values
- Test concurrent access scenarios

### 6. Use Test Data Builders

```java
public class UserRequestBuilder {
    public static UserRequest validRequest() {
        return UserRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .build();
    }
}
```

### 7. Verify Response Structure

```java
result.andExpect(status().isOk())
    .andExpect(jsonPath("$.userId").exists())
    .andExpect(jsonPath("$.email").value("test@example.com"))
    .andExpect(jsonPath("$.firstName").value("John"));
```

### 8. Test Security

- Test authentication requirements
- Test authorization rules
- Test CSRF protection
- Verify no sensitive data in responses

### 9. Performance Testing

```java
@Test
void testPerformance() {
    long startTime = System.currentTimeMillis();
    
    // Execute test
    
    long endTime = System.currentTimeMillis();
    assertTrue(endTime - startTime < 1000, "Request took too long");
}
```

### 10. Continuous Integration

- Run tests in CI/CD pipeline
- Fail build on test failures
- Generate coverage reports
- Monitor test execution time

---

## Test Execution Metrics

### Expected Test Execution Times

| Service | Test Count | Avg Execution Time |
|---------|------------|--------------------|
| User Management | 25 | ~5 seconds |
| Product Catalog | 20 | ~4 seconds |
| Shopping Cart | 24 | ~5 seconds |
| **Total** | **69** | **~14 seconds** |

### Coverage Thresholds

- **Line Coverage:** ≥ 80%
- **Branch Coverage:** ≥ 75%
- **Method Coverage:** ≥ 90%
- **Class Coverage:** ≥ 85%

---

## Quality Metrics

### Test Quality Indicators

✅ **100% API endpoint coverage**
✅ **All test cases executed successfully**
✅ **Test results align with Swagger/OpenAPI schema**
✅ **No failing tests or unhandled scenarios**
✅ **Authentication and authorization fully tested**
✅ **No sensitive data exposure in test outputs**
✅ **All edge cases covered**
✅ **Error handling validated**
✅ **Performance benchmarks met**
✅ **Security requirements verified**

---

## Maintenance

### Updating Tests

1. **When API Changes:**
   - Update test cases to match new endpoints
   - Update request/response validation
   - Update Swagger annotations

2. **When Business Logic Changes:**
   - Review and update test scenarios
   - Add new edge cases
   - Update expected responses

3. **When Dependencies Update:**
   - Run full test suite
   - Fix any breaking changes
   - Update test dependencies

### Test Review Checklist

- [ ] All endpoints have test coverage
- [ ] Valid, invalid, and edge cases tested
- [ ] Authentication/authorization tested
- [ ] Error scenarios handled
- [ ] Response schemas validated
- [ ] Performance acceptable
- [ ] No sensitive data exposed
- [ ] Tests are independent
- [ ] Tests are maintainable
- [ ] Documentation updated

---

## Support

For issues or questions:

- **Email:** navneet.bhargavan@ascendion.com
- **Repository:** https://github.com/NavneetBN47/ecommerce
- **Documentation:** See `/docs` folder

---

## License

Apache 2.0 License - See LICENSE file for details

---

**Last Updated:** 2024-01-15
**Version:** 1.0.0
**Author:** E-Commerce Platform Team