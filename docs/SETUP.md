# Setup Guide

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17+**
   - Download from: https://adoptium.net/
   - Verify installation:
   ```bash
   java -version
   javac -version
   ```

2. **Apache Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation:
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Verify installation:
   ```bash
   mysql --version
   ```

4. **Git**
   - Download from: https://git-scm.com/downloads
   - Verify installation:
   ```bash
   git --version
   ```

### Optional Software

- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **API Testing**: Postman or Insomnia
- **Database Client**: MySQL Workbench or DBeaver

## Installation Steps

### 1. Clone Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

### 2. Database Setup

#### Create Database

```bash
# Login to MySQL
mysql -u root -p
```

```sql
-- Create database
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional)
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
USE ecommerce_db;
```

#### Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=ecommerce_user
spring.datasource.password=secure_password
```

### 3. Build Project

```bash
# Clean and build
mvn clean install

# Skip tests (if needed)
mvn clean install -DskipTests
```

### 4. Run Application

#### Using Maven

```bash
mvn spring-boot:run
```

#### Using JAR

```bash
java -jar target/ecommerce-api-1.0.0.jar
```

#### Using IDE

1. Import project as Maven project
2. Run `EcommerceApplication.java`

### 5. Verify Installation

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## Configuration

### Application Properties

#### Development Profile

Create `src/main/resources/application-dev.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=root

# JPA
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.ecommerce=DEBUG
```

Run with dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Profile

Create `src/main/resources/application-prod.properties`:

```properties
# Database (use environment variables)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# JPA
spring.jpa.show-sql=false

# Logging
logging.level.com.ecommerce=INFO
```

Run with prod profile:
```bash
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

### JWT Configuration

```properties
# JWT Secret (use strong secret in production)
jwt.secret=YourVeryLongAndSecureSecretKeyForJWTTokenGeneration123456789

# JWT Expiration (24 hours in milliseconds)
jwt.expiration=86400000
```

### Database Migration

Flyway automatically runs migrations on startup.

Manual migration commands:

```bash
# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Repair failed migrations
mvn flyway:repair

# Clean database (CAUTION: Deletes all data)
mvn flyway:clean
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=AuthServiceTest
```

### Generate Test Coverage Report

```bash
mvn jacoco:report
```

View report: `target/site/jacoco/index.html`

## Sample Data

Sample data is automatically loaded via Flyway migration `V2__Sample_Data.sql`.

### Sample Users

| Email | Password | Role |
|-------|----------|------|
| john.doe@example.com | password | User |
| jane.smith@example.com | password | User |

### Sample Products

- Laptop Pro 15 - $1,299.99
- Wireless Mouse - $29.99
- USB-C Cable - $12.99
- And more...

## API Testing

### Using cURL

#### Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "1234567890"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!"
  }'
```

#### Search Products

```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "laptop",
    "page": 0,
    "size": 10
  }'
```

#### Add to Cart

```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

### Using Postman

1. Import collection from `docs/postman_collection.json`
2. Set environment variables:
   - `base_url`: http://localhost:8080
   - `token`: (will be set automatically after login)
3. Run requests in order:
   - Register → Login → Search Products → Add to Cart → Checkout

## IDE Setup

### IntelliJ IDEA

1. **Import Project**
   - File → Open → Select project directory
   - Choose "Maven" as project type

2. **Configure Lombok**
   - Install Lombok plugin
   - Enable annotation processing:
     - Settings → Build → Compiler → Annotation Processors
     - Check "Enable annotation processing"

3. **Configure Database**
   - View → Tool Windows → Database
   - Add MySQL data source
   - Test connection

4. **Run Configuration**
   - Run → Edit Configurations
   - Add new Spring Boot configuration
   - Main class: `com.ecommerce.EcommerceApplication`
   - VM options: `-Dspring.profiles.active=dev`

### Eclipse

1. **Import Project**
   - File → Import → Existing Maven Projects
   - Select project directory

2. **Install Lombok**
   - Download lombok.jar
   - Run: `java -jar lombok.jar`
   - Select Eclipse installation

3. **Run Application**
   - Right-click on `EcommerceApplication.java`
   - Run As → Java Application

### VS Code

1. **Install Extensions**
   - Java Extension Pack
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Open Project**
   - File → Open Folder
   - Select project directory

3. **Run Application**
   - Press F5
   - Select "Java" as environment

## Docker Setup (Optional)

### Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/ecommerce-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Create docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ecommerce_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ecommerce_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql

volumes:
  mysql_data:
```

### Run with Docker

```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop
docker-compose down
```

## Troubleshooting

See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed troubleshooting guide.

### Quick Fixes

#### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

#### Database Connection Failed

```bash
# Check MySQL status
sudo systemctl status mysql

# Start MySQL
sudo systemctl start mysql

# Test connection
mysql -u root -p -h localhost
```

#### Maven Build Failed

```bash
# Clean Maven cache
mvn clean

# Update dependencies
mvn clean install -U

# Skip tests
mvn clean install -DskipTests
```

## Next Steps

1. Review [API Documentation](README.md#api-documentation)
2. Explore [Database Schema](db/diagrams/er_diagram.md)
3. Read [Troubleshooting Guide](TROUBLESHOOTING.md)
4. Check [Quality Metrics](README.md#quality-metrics)
5. Start developing!

## Support

For issues or questions:
- GitHub Issues: https://github.com/NavneetBN47/ecommerce/issues
- Email: navneet.bhargavan@ascendion.com
