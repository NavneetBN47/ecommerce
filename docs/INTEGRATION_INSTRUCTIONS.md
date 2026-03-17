# Test Integration Instructions

## Overview
This document provides step-by-step instructions for integrating and running the generated unit tests for the E-Commerce microservices platform.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Running Tests Locally](#running-tests-locally)
4. [CI/CD Integration](#cicd-integration)
5. [Test Configuration](#test-configuration)
6. [Troubleshooting](#troubleshooting)
7. [Best Practices](#best-practices)

---

## Prerequisites

### Required Software
- **Java**: JDK 17 or higher
- **Maven**: 3.8.0 or higher
- **Git**: Latest version
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Verify Installation
```bash
# Check Java version
java -version
# Expected: openjdk version "17.0.x" or higher

# Check Maven version
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Check Git version
git --version
# Expected: git version 2.x.x or higher
```

---

## Project Setup

### Step 1: Clone Repository
```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
git checkout feature_2026-03-17-08-25-09
```

### Step 2: Verify Project Structure
```
ecommerce/
├── user-management-service/
│   ├── src/
│   │   ├── main/java/
│   │   └── test/java/
│   │       └── com/ecommerce/usermanagement/
│   │           └── presentation/controller/
│   │               └── UserControllerTest.java
│   └── pom.xml
├── product-catalog-service/
│   ├── src/
│   │   ├── main/java/
│   │   └── test/java/
│   │       └── com/ecommerce/productcatalog/
│   │           └── presentation/controller/
│   │               └── ProductControllerTest.java
│   └── pom.xml
├── shopping-cart-service/
│   ├── src/
│   │   ├── main/java/
│   │   └── test/java/
│   │       └── com/ecommerce/shoppingcart/
│   │           └── presentation/controller/
│   │               └── CartControllerTest.java
│   └── pom.xml
└── swagger/
    └── openapi.yaml
```

### Step 3: Install Dependencies
```bash
# User Management Service
cd user-management-service
mvn clean install -DskipTests

# Product Catalog Service
cd ../product-catalog-service
mvn clean install -DskipTests

# Shopping Cart Service
cd ../shopping-cart-service
mvn clean install -DskipTests
```

---

## Running Tests Locally

### Run All Tests for a Service

#### User Management Service
```bash
cd user-management-service
mvn clean test
```

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.ecommerce.usermanagement.presentation.controller.UserControllerTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

#### Product Catalog Service
```bash
cd product-catalog-service
mvn clean test
```

**Expected Output:**
```
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### Shopping Cart Service
```bash
cd shopping-cart-service
mvn clean test
```

**Expected Output:**
```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Run Specific Test Class
```bash
# Run only UserControllerTest
mvn test -Dtest=UserControllerTest

# Run only ProductControllerTest
mvn test -Dtest=ProductControllerTest

# Run only CartControllerTest
mvn test -Dtest=CartControllerTest
```

### Run Specific Test Method
```bash
# Run a single test method
mvn test -Dtest=UserControllerTest#testRegisterUser_Success
```

### Run Tests with Coverage Report
```bash
# Generate JaCoCo coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
# Or navigate to: target/site/jacoco/index.html in your browser
```

---

## CI/CD Integration

### GitHub Actions

Create `.github/workflows/test.yml` in your repository:

```yaml
name: Run Unit Tests

on:
  push:
    branches: [ main, develop, feature/** ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        service: [user-management-service, product-catalog-service, shopping-cart-service]
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Run tests for ${{ matrix.service }}
      run: |
        cd ${{ matrix.service }}
        mvn clean test
    
    - name: Generate coverage report
      run: |
        cd ${{ matrix.service }}
        mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./${{ matrix.service }}/target/site/jacoco/jacoco.xml
        flags: ${{ matrix.service }}
        name: ${{ matrix.service }}-coverage
    
    - name: Publish test results
      uses: EnricoMi/publish-unit-test-result-action@v2
      if: always()
      with:
        files: ./${{ matrix.service }}/target/surefire-reports/*.xml
```

### Jenkins Pipeline

Create `Jenkinsfile` in your repository:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Test User Management Service') {
            steps {
                dir('user-management-service') {
                    sh 'mvn clean test'
                }
            }
        }
        
        stage('Test Product Catalog Service') {
            steps {
                dir('product-catalog-service') {
                    sh 'mvn clean test'
                }
            }
        }
        
        stage('Test Shopping Cart Service') {
            steps {
                dir('shopping-cart-service') {
                    sh 'mvn clean test'
                }
            }
        }
        
        stage('Generate Coverage Reports') {
            steps {
                sh '''
                    cd user-management-service && mvn jacoco:report
                    cd ../product-catalog-service && mvn jacoco:report
                    cd ../shopping-cart-service && mvn jacoco:report
                '''
            }
        }
        
        stage('Publish Reports') {
            steps {
                junit '**/target/surefire-reports/*.xml'
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java'
                )
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'All tests passed successfully!'
        }
        failure {
            echo 'Tests failed. Please check the reports.'
        }
    }
}
```

---

## Test Configuration

### Maven Configuration

Ensure your `pom.xml` includes these dependencies:

```xml
<dependencies>
    <!-- Spring Boot Test -->
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
</dependencies>

<build>
    <plugins>
        <!-- Maven Surefire Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M9</version>
        </plugin>
        
        <!-- JaCoCo Plugin -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.10</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Test Properties

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
    show-sql: false
  
  security:
    jwt:
      secret: test-secret-key-for-testing-purposes-only
      expiration: 3600000

logging:
  level:
    com.ecommerce: DEBUG
    org.springframework.security: DEBUG
```

---

## Troubleshooting

### Common Issues

#### 1. Tests Fail with "Cannot find symbol"
**Solution:**
```bash
mvn clean install -DskipTests
mvn clean test
```

#### 2. Security Context Not Loading
**Solution:** Ensure `@WithMockUser` annotation is present:
```java
@Test
@WithMockUser(roles = "USER")
void testProtectedEndpoint() {
    // test code
}
```

#### 3. MockMvc Not Autowired
**Solution:** Add `@WebMvcTest` annotation:
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
}
```

#### 4. Service Bean Not Found
**Solution:** Add `@MockBean` annotation:
```java
@MockBean
private UserService userService;
```

#### 5. JSON Parsing Errors
**Solution:** Ensure ObjectMapper is autowired:
```java
@Autowired
private ObjectMapper objectMapper;
```

---

## Best Practices

### 1. Test Naming Convention
```java
// Pattern: test<MethodName>_<Scenario>
@Test
void testRegisterUser_Success() { }

@Test
void testRegisterUser_InvalidEmail() { }
```

### 2. Use Display Names
```java
@Test
@DisplayName("POST /api/v1/users/register - Success")
void testRegisterUser_Success() { }
```

### 3. Arrange-Act-Assert Pattern
```java
@Test
void testExample() {
    // Arrange
    UserDTO user = new UserDTO();
    when(service.method()).thenReturn(user);
    
    // Act
    ResponseEntity<UserDTO> response = controller.method();
    
    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service, times(1)).method();
}
```

### 4. Clean Up After Tests
```java
@AfterEach
void tearDown() {
    // Clean up resources
}
```

### 5. Use Test Fixtures
```java
@BeforeEach
void setUp() {
    // Initialize common test data
    testUser = createTestUser();
}
```

---

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Spring Security Testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html)

---

## Support

For issues or questions:
- **Email**: navneet.bhargavan@ascendion.com
- **Repository**: https://github.com/NavneetBN47/ecommerce
- **Branch**: feature_2026-03-17-08-25-09

---

**Document Version**: 1.0.0  
**Last Updated**: 2026-03-17  
**Status**: ✅ Complete
