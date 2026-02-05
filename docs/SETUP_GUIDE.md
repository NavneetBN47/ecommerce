# Complete Setup Guide - E-Commerce Shopping Cart System

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Configuration](#database-configuration)
4. [Application Configuration](#application-configuration)
5. [Building and Running](#building-and-running)
6. [Verification](#verification)
7. [IDE Setup](#ide-setup)
8. [Docker Setup (Optional)](#docker-setup-optional)
9. [Production Deployment](#production-deployment)

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   # Verify installation
   java -version
   # Should output: java version "17.x.x" or higher
   ```

   Download from:
   - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - [OpenJDK](https://openjdk.org/)
   - [Amazon Corretto](https://aws.amazon.com/corretto/)

2. **Apache Maven 3.8+**
   ```bash
   # Verify installation
   mvn -version
   # Should output: Apache Maven 3.8.x or higher
   ```

   Download from: [Apache Maven](https://maven.apache.org/download.cgi)

3. **PostgreSQL 15+**
   ```bash
   # Verify installation
   psql --version
   # Should output: psql (PostgreSQL) 15.x or higher
   ```

   Download from: [PostgreSQL](https://www.postgresql.org/download/)

4. **Git**
   ```bash
   # Verify installation
   git --version
   ```

   Download from: [Git](https://git-scm.com/downloads)

### Optional Tools

- **Postman** or **Insomnia**: For API testing
- **DBeaver** or **pgAdmin**: For database management
- **IntelliJ IDEA** or **Eclipse**: For development

## Environment Setup

### 1. Clone the Repository

```bash
# Clone the repository
git clone https://github.com/NavneetBN47/ecommerce.git

# Navigate to project directory
cd ecommerce/api-springboot
```

### 2. Set Environment Variables

#### Linux/Mac

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
export MAVEN_HOME=/path/to/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

Apply changes:
```bash
source ~/.bashrc  # or source ~/.zshrc
```

#### Windows

1. Open System Properties → Environment Variables
2. Add/Update:
   - `JAVA_HOME`: C:\Program Files\Java\jdk-17
   - `MAVEN_HOME`: C:\Program Files\Apache\maven
3. Update `PATH` to include:
   - %JAVA_HOME%\bin
   - %MAVEN_HOME%\bin

## Database Configuration

### 1. Install PostgreSQL

#### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### Mac (Homebrew)

```bash
brew install postgresql@15
brew services start postgresql@15
```

#### Windows

Download and run installer from [PostgreSQL Downloads](https://www.postgresql.org/download/windows/)

### 2. Create Database and User

```bash
# Login to PostgreSQL
sudo -u postgres psql

# Or on Windows
psql -U postgres
```

```sql
-- Create database
CREATE DATABASE ecommerce_db;

-- Create user (optional, if not using postgres user)
CREATE USER ecommerce_user WITH ENCRYPTED PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;

-- Connect to database
\c ecommerce_db

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO ecommerce_user;

-- Exit
\q
```

### 3. Verify Database Connection

```bash
psql -h localhost -U postgres -d ecommerce_db
```

If successful, you should see:
```
ecommerce_db=#
```

## Application Configuration

### 1. Configure Database Connection

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: postgres  # or ecommerce_user
    password: your_password
```

### 2. Configure JWT Secret

**Important**: Change the default JWT secret for production!

```yaml
app:
  jwt:
    secret: your-very-long-and-secure-secret-key-at-least-256-bits
    expiration-ms: 86400000  # 24 hours
```

Generate a secure secret:
```bash
# Linux/Mac
openssl rand -base64 64

# Or use online generator
# https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

### 3. Environment-Specific Configuration

#### Development (application-dev.yml)

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.ecommerce: DEBUG
```

#### Production (application-prod.yml)

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    show-sql: false

logging:
  level:
    com.ecommerce: INFO

app:
  jwt:
    secret: ${JWT_SECRET}
```

## Building and Running

### 1. Build the Application

```bash
# Clean and build
mvn clean install

# Skip tests (if needed)
mvn clean install -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### 2. Run Database Migrations

Migrations run automatically on startup, but you can run them manually:

```bash
mvn flyway:migrate
```

### 3. Start the Application

#### Development Mode

```bash
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Mode

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/shopping-cart-api-1.0.0.jar --spring.profiles.active=prod
```

#### With Custom JVM Options

```bash
java -Xms512m -Xmx2048m -jar target/shopping-cart-api-1.0.0.jar
```

### 4. Verify Application Started

Look for this in the logs:

```
Started ShoppingCartApplication in X.XXX seconds
```

Application will be available at: `http://localhost:8080`

## Verification

### 1. Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 2. Test API Endpoints

#### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123",
    "fullName": "Test User"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "Test@123"
  }'
```

Save the token from response for subsequent requests.

#### Get Products

```bash
curl http://localhost:8080/api/products
```

### 3. Verify Database

```bash
psql -U postgres -d ecommerce_db
```

```sql
-- Check tables
\dt

-- Check sample data
SELECT * FROM products LIMIT 5;
SELECT * FROM categories;

-- Exit
\q
```

## IDE Setup

### IntelliJ IDEA

1. **Import Project**
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Enable Annotation Processing**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

3. **Install Lombok Plugin**
   - Settings → Plugins → Search "Lombok" → Install

4. **Run Configuration**
   - Run → Edit Configurations → Add New → Spring Boot
   - Main class: `com.ecommerce.ShoppingCartApplication`
   - Active profiles: `dev`

### Eclipse

1. **Import Project**
   - File → Import → Existing Maven Projects
   - Select project directory

2. **Install Lombok**
   - Download lombok.jar
   - Run: `java -jar lombok.jar`
   - Select Eclipse installation

3. **Run Configuration**
   - Run → Run Configurations → Spring Boot App
   - Project: shopping-cart-api
   - Main Type: `com.ecommerce.ShoppingCartApplication`

### VS Code

1. **Install Extensions**
   - Java Extension Pack
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Open Project**
   - File → Open Folder → Select project directory

3. **Run Application**
   - Press F5 or use Run menu

## Docker Setup (Optional)

### 1. Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ecommerce_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ecommerce_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - postgres

volumes:
  postgres_data:
```

### 3. Build and Run

```bash
# Build application
mvn clean package -DskipTests

# Build and start containers
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop containers
docker-compose down
```

## Production Deployment

### 1. Build for Production

```bash
mvn clean package -DskipTests -Pprod
```

### 2. Environment Variables

Set these on your production server:

```bash
export DATABASE_URL=jdbc:postgresql://prod-db-host:5432/ecommerce_db
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=secure_password
export JWT_SECRET=your-production-secret-key
export SPRING_PROFILES_ACTIVE=prod
```

### 3. Run as Service (Linux)

Create `/etc/systemd/system/ecommerce-api.service`:

```ini
[Unit]
Description=E-Commerce Shopping Cart API
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/ecommerce-api
ExecStart=/usr/bin/java -jar /opt/ecommerce-api/shopping-cart-api-1.0.0.jar
Restart=on-failure
RestartSec=10

Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce_db"
Environment="DATABASE_USERNAME=postgres"
Environment="DATABASE_PASSWORD=your_password"
Environment="JWT_SECRET=your_jwt_secret"

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ecommerce-api
sudo systemctl start ecommerce-api
sudo systemctl status ecommerce-api
```

### 4. Nginx Reverse Proxy

Create `/etc/nginx/sites-available/ecommerce-api`:

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable and restart:

```bash
sudo ln -s /etc/nginx/sites-available/ecommerce-api /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Next Steps

1. Review [API Documentation](README.md#api-documentation)
2. Check [Troubleshooting Guide](TROUBLESHOOTING.md)
3. Set up monitoring and logging
4. Configure SSL/TLS certificates
5. Set up automated backups
6. Implement CI/CD pipeline

## Support

If you encounter any issues:

1. Check [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Review application logs
3. Create an issue on GitHub
4. Contact support team

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Docker Documentation](https://docs.docker.com/)