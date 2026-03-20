# Healthcare Management System

A HIPAA-compliant Healthcare Management System built with Spring Boot, providing comprehensive patient, appointment, and medical record management with integrated Swagger/OpenAPI documentation.

## Features

- **Patient Management**: Complete patient demographic information, medical history, and contact details
- **Appointment Scheduling**: Schedule, update, and manage patient appointments
- **Medical Records**: Secure storage and retrieval of patient medical records with encryption
- **Consent Management**: HIPAA-compliant patient consent tracking
- **Security**: JWT-based authentication and authorization
- **Audit Logging**: Comprehensive audit trails for compliance
- **API Documentation**: Interactive Swagger UI for API exploration

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Storage**: AWS S3
- **Security**: Spring Security with JWT
- **API Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+
- Apache Kafka 3.0+
- AWS Account (for S3 storage)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

### 2. Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE healthcare_db;
```

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/healthcare_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Configure Environment Variables

Set the following environment variables:

```bash
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_SECRET=your-secret-key-change-in-production
export ENCRYPTION_KEY=change-this-key-in-production
export AWS_S3_BUCKET=healthcare-documents
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Accessing Swagger UI

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification is available at:

```
http://localhost:8080/api-docs
```

## API Endpoints

### Patient Management

- `POST /api/v1/patients` - Create a new patient
- `GET /api/v1/patients/{id}` - Get patient by ID
- `GET /api/v1/patients` - Get all patients
- `PUT /api/v1/patients/{id}` - Update patient
- `DELETE /api/v1/patients/{id}` - Delete patient
- `GET /api/v1/patients/search?email={email}` - Search patient by email

### Appointment Management

- `POST /api/v1/appointments` - Schedule appointment
- `GET /api/v1/appointments/{id}` - Get appointment by ID
- `GET /api/v1/appointments/patient/{patientId}` - Get appointments by patient
- `GET /api/v1/appointments/date-range` - Get appointments by date range
- `PUT /api/v1/appointments/{id}` - Update appointment
- `PATCH /api/v1/appointments/{id}/cancel` - Cancel appointment
- `DELETE /api/v1/appointments/{id}` - Delete appointment

### Medical Record Management

- `POST /api/v1/medical-records` - Create medical record
- `GET /api/v1/medical-records/{id}` - Get medical record by ID
- `GET /api/v1/medical-records/patient/{patientId}` - Get records by patient
- `PUT /api/v1/medical-records/{id}` - Update medical record
- `DELETE /api/v1/medical-records/{id}` - Delete medical record
- `GET /api/v1/medical-records/search` - Search records by type

### Consent Management

- `POST /api/v1/consents` - Record consent
- `GET /api/v1/consents/{id}` - Get consent by ID
- `GET /api/v1/consents/patient/{patientId}` - Get consents by patient
- `PUT /api/v1/consents/{id}` - Update consent
- `PATCH /api/v1/consents/{id}/revoke` - Revoke consent
- `DELETE /api/v1/consents/{id}` - Delete consent
- `GET /api/v1/consents/check` - Check consent validity

## Authentication

All API endpoints require JWT Bearer token authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Data Encryption**: Sensitive data encrypted at rest
- **Access Control**: Role-based access control (RBAC)
- **Audit Logging**: Comprehensive audit trails
- **HIPAA Compliance**: Built-in HIPAA compliance features

## HIPAA Compliance

This system includes the following HIPAA compliance features:

- Patient consent management
- Access history tracking
- Data lineage tracking
- Audit logging
- Data encryption
- Secure authentication and authorization

## Migration Guide

### Dependencies Required

All required dependencies are included in `pom.xml`. Key dependencies:

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- SpringDoc OpenAPI UI
- PostgreSQL Driver
- Redis
- Kafka
- AWS SDK S3
- JWT Libraries

### Configuration Steps

1. **Database Setup**: Create PostgreSQL database and run schema migrations
2. **Redis Setup**: Install and start Redis server
3. **Kafka Setup**: Install and configure Kafka broker
4. **AWS S3 Setup**: Create S3 bucket and configure IAM credentials
5. **Environment Variables**: Set all required environment variables
6. **Security Configuration**: Configure JWT secret and encryption keys

### Accessing Swagger UI

1. Start the application
2. Navigate to `http://localhost:8080/swagger-ui.html`
3. Use the "Authorize" button to add your JWT token
4. Explore and test API endpoints interactively

## Testing

Run tests with:

```bash
mvn test
```

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is proprietary software. See LICENSE file for details.

## Support

For support, email support@healthcare.com or visit https://healthcare.com

## Warnings and Important Notes

- External service integrations (Auth0, Kafka, Redis, S3) require configuration
- Database connection properties must be configured in application.properties
- Encryption keys and JWT secrets must be set in environment variables
- HIPAA compliance features require additional security configuration
- Circuit breaker and resilience patterns need Resilience4j configuration

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── healthcare/
│   │           ├── config/          # Configuration classes
│   │           ├── controller/      # REST controllers
│   │           ├── dto/             # Data transfer objects
│   │           ├── entity/          # JPA entities
│   │           ├── repository/      # Data repositories
│   │           ├── service/         # Business logic
│   │           └── HealthcareApplication.java
│   └── resources/
│       ├── application.properties   # Application configuration
│       └── schema.sql              # Database schema
└── swagger/
    └── openapi.yaml                # OpenAPI specification
```

## Version History

- **1.0.0** (2024-01-15)
  - Initial release
  - Patient management
  - Appointment scheduling
  - Medical records
  - Consent management
  - Swagger integration
