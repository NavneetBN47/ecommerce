# E-Commerce Platform - API Test Suite

## Overview

This repository contains comprehensive unit test cases for all API endpoints in the E-Commerce Platform microservices architecture. The test suite ensures 100% endpoint coverage with valid, invalid, and edge case scenarios.

## Test Coverage Summary

### User Management Service (7 endpoints)
- **POST /api/v1/users/register** - 5 test cases
- **POST /api/v1/users/login** - 3 test cases
- **GET /api/v1/users/profile** - 2 test cases
- **PUT /api/v1/users/profile** - 3 test cases
- **POST /api/v1/users/logout** - 2 test cases
- **POST /api/v1/users/password/reset** - 2 test cases
- **PUT /api/v1/users/password/change** - 3 test cases

**Total: 20 test cases**

### Product Catalog Service (2 endpoints)
- **GET /api/v1/products/search** - 9 test cases
- **GET /api/v1/products/{productId}** - 6 test cases

**Total: 15 test cases**

### Shopping Cart Service (6 endpoints)
- **GET /api/v1/cart** - 3 test cases
- **POST /api/v1/cart/items** - 5 test cases
- **PUT /api/v1/cart/items/{itemId}** - 5 test cases
- **DELETE /api/v1/cart/items/{itemId}** - 4 test cases
- **DELETE /api/v1/cart** - 3 test cases

**Total: 20 test cases**

## Overall Test Statistics

- **Total Endpoints Tested:** 15
- **Total Test Cases:** 55
- **Coverage:** 100% of API endpoints
- **Test Types:** Unit tests with MockMvc and Mockito
- **Authentication Tests:** Included for protected endpoints
- **Validation Tests:** Comprehensive input validation coverage

## Technology Stack

- **Testing Framework:** JUnit 5
- **Mocking Framework:** Mockito
- **Spring Test:** MockMvc for controller testing
- **JSON Processing:** Jackson ObjectMapper
- **Security Testing:** Spring Security Test

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.9.x or higher
- Git

### Clone Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
git checkout feature_2026-03-17-08-25-09
```

### Build Project

```bash
mvn clean install
```

### Run Tests

#### Run All Tests

```bash
mvn test
```

#### Run Tests for Specific Service

```bash
# User Management Service
mvn test -pl user-management-service

# Product Catalog Service
mvn test -pl product-catalog-service

# Shopping Cart Service
mvn test -pl shopping-cart-service
```

#### Run Specific Test Class

```bash
mvn test -Dtest=UserControllerTest
mvn test -Dtest=ProductControllerTest
mvn test -Dtest=CartControllerTest
```

#### Run Tests with Coverage Report

```bash
mvn clean test jacoco:report
```

Coverage reports will be generated in:
- `user-management-service/target/site/jacoco/index.html`
- `product-catalog-service/target/site/jacoco/index.html`
- `shopping-cart-service/target/site/jacoco/index.html`

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

All test methods follow the pattern:
```
test<MethodName>_<Scenario>_<ExpectedResult>
```

Example:
```java
@Test
@DisplayName("POST /api/v1/users/register - Valid Request - Should Return 201")
void testRegisterUser_ValidRequest_ShouldReturn201() { ... }
```

## Test Categories

### 1. Valid Request Tests (Happy Path)

Tests that verify successful API responses with valid input data.

**Example:**
```java
@Test
void testRegisterUser_ValidRequest_ShouldReturn201() {
    // Arrange
    UserRegistrationRequest request = new UserRegistrationRequest(...);
    
    // Act & Assert
    mockMvc.perform(post("/api/v1/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").exists());
}
```

### 2. Invalid Request Tests (Validation)

Tests that verify proper validation error handling.

**Example:**
```java
@Test
void testRegisterUser_InvalidEmail_ShouldReturn400() {
    // Test with invalid email format
    UserRegistrationRequest request = new UserRegistrationRequest(
        "invalid-email", "SecurePass123!", "John", "Doe"
    );
    
    mockMvc.perform(post("/api/v1/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
}
```

### 3. Edge Case Tests

Tests that verify boundary conditions and special scenarios.

**Example:**
```java
@Test
void testSearchProducts_NoResults_ShouldReturn200WithEmptyList() {
    // Test search with no matching results
    mockMvc.perform(get("/api/v1/products/search")
            .param("keyword", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.products.length()").value(0));
}
```

### 4. Authentication/Authorization Tests

Tests that verify security requirements.

**Example:**
```java
@Test
@WithMockUser(username = "john.doe@example.com")
void testGetUserProfile_AuthenticatedUser_ShouldReturn200() {
    // Test with authenticated user
    mockMvc.perform(get("/api/v1/users/profile"))
        .andExpect(status().isOk());
}

@Test
void testGetUserProfile_UnauthenticatedUser_ShouldReturn401() {
    // Test without authentication
    mockMvc.perform(get("/api/v1/users/profile"))
        .andExpect(status().isUnauthorized());
}
```

## Usage Examples

### Running Tests in IDE

#### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run 'TestClassName'" or "Run 'testMethodName'"
3. View results in Run window

#### Eclipse
1. Right-click on test class or method
2. Select "Run As" > "JUnit Test"
3. View results in JUnit view

### Running Tests in CI/CD Pipeline

#### GitHub Actions Example

```yaml
name: Run Tests

on:
  push:
    branches: [ main, feature/* ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

## Troubleshooting Guide

### Common Issues

#### 1. Test Failures Due to Schema Mismatch

**Problem:** Tests fail because DTO structure doesn't match expected schema.

**Solution:**
- Compare test DTOs with Swagger/OpenAPI specification
- Update DTO classes to match API contract
- Regenerate test data with correct structure

**Example:**
```java
// Incorrect
UserRegistrationRequest request = new UserRegistrationRequest(
    "john.doe@example.com", "SecurePass123!"
);

// Correct (includes all required fields)
UserRegistrationRequest request = new UserRegistrationRequest(
    "john.doe@example.com", "SecurePass123!", "John", "Doe"
);
```

#### 2. Missing or Incorrect API Endpoint Coverage

**Problem:** Some endpoints are not tested or test URLs are incorrect.

**Solution:**
- Review Swagger UI to verify all endpoints
- Check endpoint paths in controller annotations
- Ensure test URLs match controller mappings

**Example:**
```java
// Incorrect
mockMvc.perform(get("/users/profile"))

// Correct
mockMvc.perform(get("/api/v1/users/profile"))
```

#### 3. Authentication/Authorization Test Failures

**Problem:** Tests fail due to missing or incorrect authentication setup.

**Solution:**
- Use `@WithMockUser` annotation for authenticated tests
- Verify JWT token format in integration tests
- Check Spring Security configuration

**Example:**
```java
// Add authentication to test
@Test
@WithMockUser(username = "john.doe@example.com")
void testGetUserProfile_AuthenticatedUser_ShouldReturn200() {
    mockMvc.perform(get("/api/v1/users/profile"))
        .andExpect(status().isOk());
}
```

#### 4. Invalid Request/Response Handling in Tests

**Problem:** Tests don't properly validate error responses.

**Solution:**
- Add assertions for error response structure
- Verify HTTP status codes
- Check error messages and field validation

**Example:**
```java
@Test
void testRegisterUser_InvalidEmail_ShouldReturn400() {
    mockMvc.perform(post("/api/v1/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").exists());
}
```

### Preventive Measures

1. **Maintain Alignment Between Swagger and Test Cases**
   - Regularly sync test cases with API documentation
   - Use OpenAPI specification as source of truth
   - Automate API contract testing

2. **Run Automated Tests in CI/CD Pipelines**
   - Configure GitHub Actions or Jenkins
   - Run tests on every commit and pull request
   - Block merges if tests fail

3. **Regularly Update Tests When APIs Change**
   - Update tests immediately after API changes
   - Use version control to track test changes
   - Document breaking changes

4. **Enforce Minimum Coverage Thresholds**
   - Set minimum coverage to 80%
   - Use JaCoCo Maven plugin
   - Fail builds if coverage drops below threshold

**Maven Configuration:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Quality Metrics

### Test Coverage Goals

- **Line Coverage:** ≥ 80%
- **Branch Coverage:** ≥ 75%
- **Method Coverage:** ≥ 90%
- **Class Coverage:** 100%

### Test Execution Performance

- **Average Test Execution Time:** < 5 seconds per test class
- **Total Test Suite Execution Time:** < 2 minutes
- **Parallel Execution:** Enabled for faster feedback

### Test Reliability

- **Flaky Test Rate:** < 1%
- **Test Failure Rate:** 0% on main branch
- **Test Maintenance Effort:** < 10% of development time

## Best Practices

### 1. Test Independence

Each test should be independent and not rely on other tests.

```java
@BeforeEach
void setUp() {
    // Initialize test data for each test
    validRequest = new UserRegistrationRequest(...);
}
```

### 2. Clear Test Names

Use descriptive test names that explain the scenario and expected outcome.

```java
@Test
@DisplayName("POST /api/v1/users/register - Invalid Email - Should Return 400")
void testRegisterUser_InvalidEmail_ShouldReturn400() { ... }
```

### 3. Arrange-Act-Assert Pattern

Structure tests with clear sections.

```java
@Test
void testExample() {
    // Arrange
    UserRegistrationRequest request = new UserRegistrationRequest(...);
    
    // Act
    ResultActions result = mockMvc.perform(post("/api/v1/users/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)));
    
    // Assert
    result.andExpect(status().isCreated())
          .andExpect(jsonPath("$.userId").exists());
}
```

### 4. Mock External Dependencies

Use Mockito to mock service layer dependencies.

```java
@MockBean
private UserService userService;

@Test
void testExample() {
    when(userService.registerUser(any(UserRegistrationRequest.class)))
        .thenReturn(expectedResponse);
    
    // Test controller logic
}
```

### 5. Test Edge Cases

Include tests for boundary conditions and special scenarios.

```java
@Test
void testAddItemToCart_QuantityAtMinimum_ShouldReturn201() {
    AddCartItemRequest request = new AddCartItemRequest(productId, 1);
    // Test with minimum quantity
}

@Test
void testAddItemToCart_QuantityAtMaximum_ShouldReturn201() {
    AddCartItemRequest request = new AddCartItemRequest(productId, 99);
    // Test with maximum quantity
}
```

## Contributing

### Adding New Tests

1. Create test method following naming convention
2. Add `@Test` and `@DisplayName` annotations
3. Implement Arrange-Act-Assert pattern
4. Run test locally to verify
5. Commit with descriptive message

### Updating Existing Tests

1. Identify test to update
2. Make necessary changes
3. Verify all related tests still pass
4. Update documentation if needed
5. Commit with explanation of changes

## Support

For questions or issues:

- **Email:** navneet.bhargavan@ascendion.com
- **GitHub Issues:** https://github.com/NavneetBN47/ecommerce/issues
- **Documentation:** See `/docs` folder for detailed API documentation

## License

This project is licensed under the Apache License 2.0 - see LICENSE file for details.

## Acknowledgments

- Spring Boot Team for excellent testing support
- JUnit and Mockito communities
- E-Commerce Platform development team

---

**Last Updated:** 2024-01-15

**Version:** 1.0.0

**Status:** ✅ All tests passing | 100% endpoint coverage | 55 test cases