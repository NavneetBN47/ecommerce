# LOW-LEVEL DESIGN DOCUMENT
## Healthcare Patient Management System

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|----------|
| 1.0.0 | 2024-01-20 | Senior Backend Solution Architect | Initial LLD Release |

**Document Classification:** Confidential - Internal Use Only

**Compliance Standards:** HIPAA, HITECH, GDPR, SOC2, ISO27001

**Architecture Certification:** TOGAF 9.2 Compliant

---

## TABLE OF CONTENTS

1. [Executive Summary](#1-executive-summary)
2. [System Architecture Overview](#2-system-architecture-overview)
3. [Component Design](#3-component-design)
4. [Data Architecture](#4-data-architecture)
5. [API Interface Specifications](#5-api-interface-specifications)
6. [Security Architecture](#6-security-architecture)
7. [Integration Architecture](#7-integration-architecture)
8. [Deployment Architecture](#8-deployment-architecture)
9. [Operational Procedures](#9-operational-procedures)
10. [Compliance & Governance](#10-compliance--governance)
11. [Appendices](#11-appendices)

---

## 1. EXECUTIVE SUMMARY

### 1.1 Purpose

This Low-Level Design (LLD) document provides comprehensive technical specifications for implementing the Healthcare Patient Management System API. It translates the validated API design specification into detailed system components, interfaces, data models, and operational procedures required for enterprise deployment.

### 1.2 Scope

The system encompasses three primary service domains:

- **Patient Management Service**: Patient registration, demographics, consent management
- **Appointment Service**: Scheduling, cancellation, availability management
- **Medical Records Service**: EHR management, clinical documentation, medical history

### 1.3 Key Design Principles

1. **Security First**: HIPAA-compliant with defense-in-depth security architecture
2. **High Availability**: 99.9% uptime SLA with multi-region deployment
3. **Scalability**: Horizontal scaling to support 10,000+ concurrent users
4. **Compliance**: Built-in audit logging, data lineage, and regulatory reporting
5. **Resilience**: Circuit breakers, retry policies, graceful degradation
6. **Observability**: Comprehensive monitoring, logging, and tracing

### 1.4 Technology Stack

| Layer | Technology | Version | Justification |
|-------|-----------|---------|---------------|
| API Gateway | Kong | 3.x | Enterprise features, plugin ecosystem |
| Application | Node.js + NestJS | 20.x LTS / 10.x | TypeScript support, enterprise patterns |
| Database | PostgreSQL | 15.x | ACID compliance, JSON support |
| Cache | Redis | 7.x | High performance, pub/sub support |
| Message Queue | Apache Kafka | 3.x | Event streaming, high throughput |
| Search | Elasticsearch | 8.x | Full-text search, analytics |
| Authentication | Auth0 | Enterprise | OAuth 2.0, SAML, MFA support |
| Monitoring | Prometheus + Grafana | Latest | Open-source, flexible |
| Logging | ELK Stack | 8.x | Centralized logging, analysis |
| Tracing | Jaeger | Latest | OpenTelemetry compatible |
| Container | Docker | 24.x | Standardized packaging |
| Orchestration | Kubernetes | 1.28.x | Industry standard, cloud-agnostic |

---

## 2. SYSTEM ARCHITECTURE OVERVIEW

### 2.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          External Clients                            │
│  (Web App, Mobile App, Third-Party Systems, Healthcare Providers)   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTPS/TLS 1.3
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         CDN (CloudFront)                             │
│                    (Static Content, Edge Caching)                    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      WAF (Web Application Firewall)                  │
│              (DDoS Protection, Rate Limiting, IP Filtering)          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Application Load Balancer                         │
│                  (SSL Termination, Health Checks)                    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         API Gateway (Kong)                           │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ • Authentication & Authorization (OAuth 2.0 + JWT)           │  │
│  │ • Rate Limiting & Throttling                                 │  │
│  │ • Request/Response Transformation                            │  │
│  │ • API Versioning & Routing                                   │  │
│  │ • Logging & Monitoring                                       │  │
│  │ • Circuit Breaking                                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │   Patient   │  │ Appointment │  │   Medical   │
    │  Management │  │   Service   │  │   Records   │
    │   Service   │  │             │  │   Service   │
    └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
           │                │                │
           └────────────────┼────────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │  PostgreSQL │  │    Redis    │  │   Kafka     │
    │  (Primary)  │  │   (Cache)   │  │ (Events)    │
    └─────────────┘  └─────────────┘  └─────────────┘
           │
           ▼
    ┌─────────────┐
    │  PostgreSQL │
    │ (Read Replica)│
    └─────────────┘

    ┌─────────────────────────────────────────────┐
    │         Supporting Services                  │
    ├─────────────────────────────────────────────┤
    │ • Auth Service (Auth0)                      │
    │ • Notification Service (Email/SMS)          │
    │ • Audit Service (Centralized Logging)       │
    │ • File Storage (S3/Azure Blob)              │
    │ • Search Service (Elasticsearch)            │
    └─────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────┐
    │      Observability Stack                     │
    ├─────────────────────────────────────────────┤
    │ • Prometheus (Metrics)                      │
    │ • Grafana (Visualization)                   │
    │ • ELK Stack (Logging)                       │
    │ • Jaeger (Distributed Tracing)              │
    └─────────────────────────────────────────────┘
```

### 2.2 Service Decomposition

#### 2.2.1 Patient Management Service

**Responsibilities:**
- Patient CRUD operations
- Demographics management
- Consent management
- Patient search and retrieval

**Key Components:**
- PatientController
- PatientService
- ConsentService
- PatientRepository
- ConsentRepository

**Data Entities:**
- Patient
- PatientConsent
- PatientAddress
- EmergencyContact
- InsuranceInfo

#### 2.2.2 Appointment Service

**Responsibilities:**
- Appointment scheduling
- Availability management
- Appointment lifecycle management
- Notification triggers

**Key Components:**
- AppointmentController
- AppointmentService
- AvailabilityService
- AppointmentRepository
- NotificationAdapter

**Data Entities:**
- Appointment
- TimeSlot
- AppointmentStatus
- CancellationReason

#### 2.2.3 Medical Records Service

**Responsibilities:**
- EHR management
- Clinical documentation
- Medical history retrieval
- Document storage

**Key Components:**
- MedicalRecordController
- MedicalRecordService
- DocumentService
- MedicalRecordRepository
- FileStorageAdapter

**Data Entities:**
- MedicalRecord
- ClinicalNote
- Diagnosis
- Prescription
- LabResult
- Attachment

### 2.3 Cross-Cutting Concerns

#### 2.3.1 Security Layer
- Authentication Filter
- Authorization Filter
- Encryption Service
- Audit Logger
- Security Context

#### 2.3.2 Resilience Layer
- Circuit Breaker
- Retry Handler
- Timeout Manager
- Bulkhead Isolator

#### 2.3.3 Observability Layer
- Metrics Collector
- Log Aggregator
- Trace Context Propagator
- Health Check Manager

---

## 3. COMPONENT DESIGN

### 3.1 Patient Management Service

#### 3.1.1 Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│              Patient Management Service                      │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │           Presentation Layer                        │    │
│  │  ┌──────────────────┐  ┌──────────────────┐        │    │
│  │  │ PatientController│  │ ConsentController│        │    │
│  │  └────────┬─────────┘  └────────┬─────────┘        │    │
│  └───────────┼────────────────────┼──────────────────┘    │
│              │                    │                         │
│  ┌───────────┼────────────────────┼──────────────────┐    │
│  │           │  Business Layer    │                  │    │
│  │  ┌────────▼─────────┐  ┌───────▼──────────┐      │    │
│  │  │  PatientService  │  │  ConsentService  │      │    │
│  │  │                  │  │                  │      │    │
│  │  │ • createPatient  │  │ • updateConsent  │      │    │
│  │  │ • updatePatient  │  │ • getConsent     │      │    │
│  │  │ • getPatient     │  │ • validateConsent│      │    │
│  │  │ • searchPatients │  │                  │      │    │
│  │  │ • deletePatient  │  │                  │      │    │
│  │  └────────┬─────────┘  └───────┬──────────┘      │    │
│  └───────────┼────────────────────┼──────────────────┘    │
│              │                    │                         │
│  ┌───────────┼────────────────────┼──────────────────┐    │
│  │           │  Data Access Layer │                  │    │
│  │  ┌────────▼─────────┐  ┌───────▼──────────┐      │    │
│  │  │PatientRepository │  │ConsentRepository │      │    │
│  │  │                  │  │                  │      │    │
│  │  │ • save()         │  │ • save()         │      │    │
│  │  │ • findById()     │  │ • findByPatient()│      │    │
│  │  │ • findByEmail()  │  │ • update()       │      │    │
│  │  │ • search()       │  │                  │      │    │
│  │  │ • delete()       │  │                  │      │    │
│  │  └────────┬─────────┘  └───────┬──────────┘      │    │
│  └───────────┼────────────────────┼──────────────────┘    │
│              │                    │                         │
│  ┌───────────┼────────────────────┼──────────────────┐    │
│  │           │  Infrastructure    │                  │    │
│  │  ┌────────▼─────────┐  ┌───────▼──────────┐      │    │
│  │  │   PostgreSQL     │  │   Redis Cache    │      │    │
│  │  └──────────────────┘  └──────────────────┘      │    │
│  └─────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐  │
│  │           Cross-Cutting Concerns                     │  │
│  │  • SecurityFilter                                    │  │
│  │  • AuditLogger                                       │  │
│  │  • ExceptionHandler                                  │  │
│  │  • ValidationService                                 │  │
│  │  • EncryptionService                                 │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### 3.1.2 Class Diagram

```typescript
// ============= CONTROLLERS =============

@Controller('/v1/patients')
@UseGuards(AuthGuard, RBACGuard)
export class PatientController {
  constructor(
    private readonly patientService: PatientService,
    private readonly auditLogger: AuditLogger
  ) {}

  @Post()
  @RequirePermission('write:patients')
  @UseInterceptors(IdempotencyInterceptor)
  async createPatient(
    @Body() request: CreatePatientRequest,
    @Headers('idempotency-key') idempotencyKey: string,
    @Headers('x-request-id') requestId: string,
    @CurrentUser() user: User
  ): Promise<PatientResponse> {
    // Implementation
  }

  @Get(':patientId')
  @RequirePermission('read:patients')
  async getPatient(
    @Param('patientId') patientId: string,
    @Headers('x-request-id') requestId: string,
    @CurrentUser() user: User
  ): Promise<PatientResponse> {
    // Implementation
  }

  @Put(':patientId')
  @RequirePermission('write:patients')
  @UseInterceptors(OptimisticLockingInterceptor)
  async updatePatient(
    @Param('patientId') patientId: string,
    @Body() request: UpdatePatientRequest,
    @Headers('if-match') etag: string,
    @Headers('idempotency-key') idempotencyKey: string,
    @CurrentUser() user: User
  ): Promise<PatientResponse> {
    // Implementation
  }

  @Get()
  @RequirePermission('read:patients')
  async searchPatients(
    @Query() query: PatientSearchQuery,
    @CurrentUser() user: User
  ): Promise<PatientSearchResponse> {
    // Implementation
  }

  @Delete(':patientId')
  @RequirePermission('write:patients')
  @RequireRole('admin')
  async deletePatient(
    @Param('patientId') patientId: string,
    @CurrentUser() user: User
  ): Promise<void> {
    // Implementation
  }
}

// ============= SERVICES =============

@Injectable()
export class PatientService {
  constructor(
    private readonly patientRepository: PatientRepository,
    private readonly consentService: ConsentService,
    private readonly encryptionService: EncryptionService,
    private readonly eventPublisher: EventPublisher,
    private readonly cacheManager: CacheManager,
    private readonly auditLogger: AuditLogger
  ) {}

  async createPatient(
    request: CreatePatientRequest,
    context: RequestContext
  ): Promise<Patient> {
    // 1. Validate input
    await this.validatePatientData(request);

    // 2. Check for duplicates
    await this.checkDuplicatePatient(request.email, request.ssn);

    // 3. Encrypt sensitive data
    const encryptedSSN = await this.encryptionService.encrypt(request.ssn);

    // 4. Generate MRN
    const mrn = await this.generateMRN();

    // 5. Create patient entity
    const patient = new Patient({
      ...request,
      ssn: encryptedSSN,
      mrn,
      status: PatientStatus.ACTIVE,
      createdBy: context.userId,
      createdAt: new Date()
    });

    // 6. Save to database
    const savedPatient = await this.patientRepository.save(patient);

    // 7. Create consent record
    await this.consentService.createConsent(
      savedPatient.id,
      request.consent,
      context
    );

    // 8. Publish event
    await this.eventPublisher.publish(
      new PatientCreatedEvent(savedPatient)
    );

    // 9. Audit log
    await this.auditLogger.log({
      eventType: 'PATIENT_CREATED',
      resourceId: savedPatient.id,
      userId: context.userId,
      timestamp: new Date()
    });

    return savedPatient;
  }

  async getPatient(
    patientId: string,
    context: RequestContext
  ): Promise<Patient> {
    // 1. Check cache
    const cached = await this.cacheManager.get(`patient:${patientId}`);
    if (cached) {
      await this.auditLogger.log({
        eventType: 'PATIENT_ACCESSED',
        resourceId: patientId,
        userId: context.userId,
        source: 'CACHE'
      });
      return cached;
    }

    // 2. Fetch from database
    const patient = await this.patientRepository.findById(patientId);
    if (!patient) {
      throw new NotFoundException(`Patient ${patientId} not found`);
    }

    // 3. Check access permissions
    await this.checkAccessPermissions(patient, context);

    // 4. Decrypt sensitive data
    patient.ssn = await this.encryptionService.decrypt(patient.ssn);

    // 5. Mask sensitive data based on role
    const maskedPatient = await this.maskSensitiveData(patient, context);

    // 6. Cache result
    await this.cacheManager.set(
      `patient:${patientId}`,
      maskedPatient,
      300 // 5 minutes TTL
    );

    // 7. Audit log
    await this.auditLogger.log({
      eventType: 'PATIENT_ACCESSED',
      resourceId: patientId,
      userId: context.userId,
      source: 'DATABASE'
    });

    return maskedPatient;
  }

  async updatePatient(
    patientId: string,
    request: UpdatePatientRequest,
    etag: string,
    context: RequestContext
  ): Promise<Patient> {
    // 1. Fetch existing patient
    const existingPatient = await this.patientRepository.findById(patientId);
    if (!existingPatient) {
      throw new NotFoundException(`Patient ${patientId} not found`);
    }

    // 2. Check optimistic locking
    if (existingPatient.version !== this.parseETag(etag)) {
      throw new PreconditionFailedException('Patient has been modified');
    }

    // 3. Check access permissions
    await this.checkAccessPermissions(existingPatient, context);

    // 4. Validate updates
    await this.validatePatientData(request);

    // 5. Encrypt sensitive data if changed
    if (request.ssn && request.ssn !== existingPatient.ssn) {
      request.ssn = await this.encryptionService.encrypt(request.ssn);
    }

    // 6. Update patient
    const updatedPatient = await this.patientRepository.update(
      patientId,
      {
        ...request,
        updatedBy: context.userId,
        updatedAt: new Date(),
        version: existingPatient.version + 1
      }
    );

    // 7. Invalidate cache
    await this.cacheManager.delete(`patient:${patientId}`);

    // 8. Publish event
    await this.eventPublisher.publish(
      new PatientUpdatedEvent(updatedPatient, existingPatient)
    );

    // 9. Audit log
    await this.auditLogger.log({
      eventType: 'PATIENT_UPDATED',
      resourceId: patientId,
      userId: context.userId,
      changes: this.calculateChanges(existingPatient, updatedPatient)
    });

    return updatedPatient;
  }

  async searchPatients(
    query: PatientSearchQuery,
    context: RequestContext
  ): Promise<PaginatedResult<Patient>> {
    // 1. Build search criteria
    const criteria = this.buildSearchCriteria(query, context);

    // 2. Apply ABAC filters
    const filteredCriteria = await this.applyABACFilters(criteria, context);

    // 3. Execute search
    const results = await this.patientRepository.search(
      filteredCriteria,
      query.page,
      query.pageSize
    );

    // 4. Mask sensitive data
    const maskedResults = await Promise.all(
      results.data.map(patient => this.maskSensitiveData(patient, context))
    );

    // 5. Audit log
    await this.auditLogger.log({
      eventType: 'PATIENT_SEARCH',
      userId: context.userId,
      criteria: filteredCriteria,
      resultCount: results.totalItems
    });

    return {
      data: maskedResults,
      pagination: results.pagination
    };
  }

  async deletePatient(
    patientId: string,
    context: RequestContext
  ): Promise<void> {
    // 1. Fetch patient
    const patient = await this.patientRepository.findById(patientId);
    if (!patient) {
      throw new NotFoundException(`Patient ${patientId} not found`);
    }

    // 2. Check permissions (admin only)
    if (!context.roles.includes('admin')) {
      throw new ForbiddenException('Insufficient permissions');
    }

    // 3. Soft delete (mark as inactive)
    await this.patientRepository.update(patientId, {
      status: PatientStatus.INACTIVE,
      deletedBy: context.userId,
      deletedAt: new Date()
    });

    // 4. Invalidate cache
    await this.cacheManager.delete(`patient:${patientId}`);

    // 5. Publish event
    await this.eventPublisher.publish(
      new PatientDeletedEvent(patient)
    );

    // 6. Audit log
    await this.auditLogger.log({
      eventType: 'PATIENT_DELETED',
      resourceId: patientId,
      userId: context.userId,
      severity: 'CRITICAL'
    });
  }

  // Private helper methods
  private async validatePatientData(data: any): Promise<void> {
    // Validation logic
  }

  private async checkDuplicatePatient(
    email: string,
    ssn: string
  ): Promise<void> {
    // Duplicate check logic
  }

  private async generateMRN(): Promise<string> {
    // MRN generation logic
  }

  private async checkAccessPermissions(
    patient: Patient,
    context: RequestContext
  ): Promise<void> {
    // ABAC permission check
  }

  private async maskSensitiveData(
    patient: Patient,
    context: RequestContext
  ): Promise<Patient> {
    // Data masking logic based on role
  }

  private buildSearchCriteria(
    query: PatientSearchQuery,
    context: RequestContext
  ): SearchCriteria {
    // Search criteria builder
  }

  private async applyABACFilters(
    criteria: SearchCriteria,
    context: RequestContext
  ): Promise<SearchCriteria> {
    // ABAC filter application
  }

  private calculateChanges(
    oldPatient: Patient,
    newPatient: Patient
  ): ChangeSet {
    // Change calculation for audit
  }

  private parseETag(etag: string): number {
    // ETag parsing logic
  }
}

// ============= REPOSITORIES =============

@Injectable()
export class PatientRepository {
  constructor(
    @InjectRepository(PatientEntity)
    private readonly repository: Repository<PatientEntity>,
    private readonly queryBuilder: QueryBuilder
  ) {}

  async save(patient: Patient): Promise<Patient> {
    const entity = this.toEntity(patient);
    const saved = await this.repository.save(entity);
    return this.toDomain(saved);
  }

  async findById(id: string): Promise<Patient | null> {
    const entity = await this.repository.findOne({
      where: { id, status: Not(PatientStatus.DELETED) },
      relations: ['address', 'emergencyContact', 'insuranceInfo']
    });
    return entity ? this.toDomain(entity) : null;
  }

  async findByEmail(email: string): Promise<Patient | null> {
    const entity = await this.repository.findOne({
      where: { email, status: Not(PatientStatus.DELETED) }
    });
    return entity ? this.toDomain(entity) : null;
  }

  async search(
    criteria: SearchCriteria,
    page: number,
    pageSize: number
  ): Promise<PaginatedResult<Patient>> {
    const query = this.queryBuilder.build(criteria);
    const [entities, total] = await this.repository.findAndCount({
      ...query,
      skip: (page - 1) * pageSize,
      take: pageSize
    });

    return {
      data: entities.map(e => this.toDomain(e)),
      pagination: {
        page,
        pageSize,
        totalPages: Math.ceil(total / pageSize),
        totalItems: total
      }
    };
  }

  async update(id: string, updates: Partial<Patient>): Promise<Patient> {
    await this.repository.update(id, this.toEntity(updates));
    return this.findById(id);
  }

  async delete(id: string): Promise<void> {
    await this.repository.softDelete(id);
  }

  private toEntity(domain: Patient): PatientEntity {
    // Domain to entity mapping
  }

  private toDomain(entity: PatientEntity): Patient {
    // Entity to domain mapping
  }
}

// ============= DOMAIN MODELS =============

export class Patient {
  id: string;
  mrn: string;
  firstName: string;
  middleName?: string;
  lastName: string;
  dateOfBirth: Date;
  gender: Gender;
  ssn: string; // Encrypted
  email: string;
  phone: string;
  address: Address;
  emergencyContact: EmergencyContact;
  insuranceInfo?: InsuranceInfo;
  status: PatientStatus;
  consent: ConsentData;
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
  updatedBy?: string;
  deletedAt?: Date;
  deletedBy?: string;
  version: number;

  constructor(data: Partial<Patient>) {
    Object.assign(this, data);
  }

  // Business logic methods
  isActive(): boolean {
    return this.status === PatientStatus.ACTIVE;
  }

  hasConsent(consentType: ConsentType): boolean {
    return this.consent[consentType] === true;
  }

  getAge(): number {
    const today = new Date();
    const birthDate = new Date(this.dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  getMaskedSSN(): string {
    return `XXX-XX-${this.ssn.slice(-4)}`;
  }
}

export class Address {
  street: string;
  street2?: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export class EmergencyContact {
  name: string;
  relationship: string;
  phone: string;
  email?: string;
}

export class InsuranceInfo {
  provider: string;
  policyNumber: string; // Encrypted
  groupNumber: string;
  effectiveDate: Date;
  expirationDate: Date;
}

export class ConsentData {
  treatmentConsent: boolean;
  dataProcessingConsent: boolean;
  marketingConsent: boolean;
  researchConsent: boolean;
  consentDate: Date;
  consentVersion: string;
  ipAddress: string;
}

export enum PatientStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DECEASED = 'DECEASED',
  DELETED = 'DELETED'
}

export enum Gender {
  MALE = 'M',
  FEMALE = 'F',
  OTHER = 'O',
  UNKNOWN = 'U'
}

export enum ConsentType {
  TREATMENT = 'treatmentConsent',
  DATA_PROCESSING = 'dataProcessingConsent',
  MARKETING = 'marketingConsent',
  RESEARCH = 'researchConsent'
}
```

#### 3.1.3 Sequence Diagrams

##### Create Patient Flow

```
Client          API Gateway    PatientController   PatientService   PatientRepository   Database   EventPublisher   AuditLogger
  │                  │                 │                  │                  │              │              │              │
  │  POST /patients  │                 │                  │                  │              │              │              │
  ├─────────────────>│                 │                  │                  │              │              │              │
  │                  │  Authenticate   │                  │                  │              │              │              │
  │                  ├────────────────>│                  │                  │              │              │              │
  │                  │  Authorize      │                  │                  │              │              │              │
  │                  ├────────────────>│                  │                  │              │              │              │
  │                  │  Rate Limit     │                  │                  │              │              │              │
  │                  ├────────────────>│                  │                  │              │              │              │
  │                  │                 │  createPatient() │                  │              │              │              │
  │                  │                 ├─────────────────>│                  │              │              │              │
  │                  │                 │                  │  validate()      │              │              │              │
  │                  │                 │                  ├─────────────────>│              │              │              │
  │                  │                 │                  │  checkDuplicate()│              │              │              │
  │                  │                 │                  ├─────────────────>│              │              │              │
  │                  │                 │                  │                  │  SELECT      │              │              │
  │                  │                 │                  │                  ├─────────────>│              │              │
  │                  │                 │                  │                  │  Result      │              │              │
  │                  │                 │                  │                  │<─────────────┤              │              │
  │                  │                 │                  │  encrypt()       │              │              │              │
  │                  │                 │                  ├─────────────────>│              │              │              │
  │                  │                 │                  │  generateMRN()   │              │              │              │
  │                  │                 │                  ├─────────────────>│              │              │              │
  │                  │                 │                  │  save()          │              │              │              │
  │                  │                 │                  ├─────────────────>│              │              │              │
  │                  │                 │                  │                  │  INSERT      │              │              │
  │                  │                 │                  │                  ├─────────────>│              │              │
  │                  │                 │                  │                  │  Patient     │              │              │
  │                  │                 │                  │                  │<─────────────┤              │              │
  │                  │                 │                  │  Patient         │              │              │              │
  │                  │                 │                  │<─────────────────┤              │              │              │
  │                  │                 │                  │  publish(PatientCreatedEvent)   │              │              │
  │                  │                 │                  ├────────────────────────────────>│              │              │
  │                  │                 │                  │  log(PATIENT_CREATED)           │              │              │
  │                  │                 │                  ├───────────────────────────────────────────────>│              │
  │                  │                 │  PatientResponse │                  │              │              │              │
  │                  │                 │<─────────────────┤                  │              │              │              │
  │                  │  201 Created    │                  │                  │              │              │              │
  │                  │<────────────────┤                  │                  │              │              │              │
  │  201 Created     │                 │                  │                  │              │              │              │
  │<─────────────────┤                 │                  │                  │              │              │              │
```

##### Get Patient Flow (with Caching)

```
Client          API Gateway    PatientController   PatientService   CacheManager   PatientRepository   Database   AuditLogger
  │                  │                 │                  │                │                  │              │              │
  │  GET /patients/123                 │                  │                │                  │              │              │
  ├─────────────────>│                 │                  │                │                  │              │              │
  │                  │  Authenticate   │                  │                │                  │              │              │
  │                  ├────────────────>│                  │                │                  │              │              │
  │                  │  Authorize      │                  │                │                  │              │              │
  │                  ├────────────────>│                  │                │                  │              │              │
  │                  │                 │  getPatient()    │                │                  │              │              │
  │                  │                 ├─────────────────>│                │                  │              │              │
  │                  │                 │                  │  get(patient:123)                │              │              │
  │                  │                 │                  ├───────────────>│                  │              │              │
  │                  │                 │                  │  Cache Hit     │                  │              │              │
  │                  │                 │                  │<───────────────┤                  │              │              │
  │                  │                 │                  │  log(PATIENT_ACCESSED, source=CACHE)           │              │
  │                  │                 │                  ├───────────────────────────────────────────────>│              │
  │                  │                 │  PatientResponse │                │                  │              │              │
  │                  │                 │<─────────────────┤                │                  │              │              │
  │                  │  200 OK         │                  │                │                  │              │              │
  │                  │<────────────────┤                  │                │                  │              │              │
  │  200 OK          │                 │                  │                │                  │              │              │
  │<─────────────────┤                 │                  │                │                  │              │              │

  [Alternative: Cache Miss]
  │                  │                 │                  │  get(patient:123)                │              │              │
  │                  │                 │                  ├───────────────>│                  │              │              │
  │                  │                 │                  │  Cache Miss    │                  │              │              │
  │                  │                 │                  │<───────────────┤                  │              │              │
  │                  │                 │                  │  findById()    │                  │              │              │
  │                  │                 │                  ├───────────────────────────────────>│              │              │
  │                  │                 │                  │                │                  │  SELECT      │              │
  │                  │                 │                  │                │                  ├─────────────>│              │
  │                  │                 │                  │                │                  │  Patient     │              │
  │                  │                 │                  │                │                  │<─────────────┤              │
  │                  │                 │                  │  Patient       │                  │              │              │
  │                  │                 │                  │<───────────────────────────────────┤              │              │
  │                  │                 │                  │  decrypt()     │                  │              │              │
  │                  │                 │                  │  maskData()    │                  │              │              │
  │                  │                 │                  │  set(patient:123, data, TTL=300) │              │              │
  │                  │                 │                  ├───────────────>│                  │              │              │
  │                  │                 │                  │  log(PATIENT_ACCESSED, source=DATABASE)        │              │
  │                  │                 │                  ├───────────────────────────────────────────────>│              │
```

### 3.2 Appointment Service

#### 3.2.1 Component Structure

```typescript
// ============= CONTROLLERS =============

@Controller('/v1/appointments')
@UseGuards(AuthGuard, RBACGuard)
export class AppointmentController {
  constructor(
    private readonly appointmentService: AppointmentService,
    private readonly auditLogger: AuditLogger
  ) {}

  @Post()
  @RequirePermission('write:appointments')
  @UseInterceptors(IdempotencyInterceptor)
  async scheduleAppointment(
    @Body() request: CreateAppointmentRequest,
    @Headers('idempotency-key') idempotencyKey: string,
    @CurrentUser() user: User
  ): Promise<AppointmentResponse> {
    // Implementation
  }

  @Get(':appointmentId')
  @RequirePermission('read:appointments')
  async getAppointment(
    @Param('appointmentId') appointmentId: string,
    @CurrentUser() user: User
  ): Promise<AppointmentResponse> {
    // Implementation
  }

  @Patch(':appointmentId')
  @RequirePermission('write:appointments')
  async updateAppointment(
    @Param('appointmentId') appointmentId: string,
    @Body() request: UpdateAppointmentRequest,
    @CurrentUser() user: User
  ): Promise<AppointmentResponse> {
    // Implementation
  }

  @Post(':appointmentId/cancel')
  @RequirePermission('write:appointments')
  async cancelAppointment(
    @Param('appointmentId') appointmentId: string,
    @Body() request: CancelAppointmentRequest,
    @CurrentUser() user: User
  ): Promise<AppointmentResponse> {
    // Implementation
  }

  @Get()
  @RequirePermission('read:appointments')
  async listAppointments(
    @Query() query: AppointmentListQuery,
    @CurrentUser() user: User
  ): Promise<AppointmentListResponse> {
    // Implementation
  }
}

// ============= SERVICES =============

@Injectable()
export class AppointmentService {
  constructor(
    private readonly appointmentRepository: AppointmentRepository,
    private readonly availabilityService: AvailabilityService,
    private readonly patientService: PatientService,
    private readonly notificationAdapter: NotificationAdapter,
    private readonly eventPublisher: EventPublisher,
    private readonly auditLogger: AuditLogger
  ) {}

  async scheduleAppointment(
    request: CreateAppointmentRequest,
    context: RequestContext
  ): Promise<Appointment> {
    // 1. Validate patient exists
    const patient = await this.patientService.getPatient(
      request.patientId,
      context
    );
    if (!patient) {
      throw new NotFoundException('Patient not found');
    }

    // 2. Check provider availability
    const isAvailable = await this.availabilityService.checkAvailability(
      request.providerId,
      request.scheduledDateTime,
      request.durationMinutes
    );
    if (!isAvailable) {
      throw new ConflictException('Provider not available at requested time');
    }

    // 3. Check for conflicts
    const hasConflict = await this.checkAppointmentConflicts(
      request.patientId,
      request.scheduledDateTime
    );
    if (hasConflict) {
      throw new ConflictException('Patient has conflicting appointment');
    }

    // 4. Create appointment
    const appointment = new Appointment({
      ...request,
      status: AppointmentStatus.SCHEDULED,
      createdBy: context.userId,
      createdAt: new Date()
    });

    // 5. Save to database
    const savedAppointment = await this.appointmentRepository.save(appointment);

    // 6. Block time slot
    await this.availabilityService.blockTimeSlot(
      request.providerId,
      request.scheduledDateTime,
      request.durationMinutes
    );

    // 7. Send notifications
    await this.notificationAdapter.sendAppointmentConfirmation(
      patient.email,
      savedAppointment
    );

    // 8. Publish event
    await this.eventPublisher.publish(
      new AppointmentScheduledEvent(savedAppointment)
    );

    // 9. Audit log
    await this.auditLogger.log({
      eventType: 'APPOINTMENT_SCHEDULED',
      resourceId: savedAppointment.id,
      userId: context.userId
    });

    return savedAppointment;
  }

  async cancelAppointment(
    appointmentId: string,
    request: CancelAppointmentRequest,
    context: RequestContext
  ): Promise<Appointment> {
    // 1. Fetch appointment
    const appointment = await this.appointmentRepository.findById(appointmentId);
    if (!appointment) {
      throw new NotFoundException('Appointment not found');
    }

    // 2. Check if already cancelled
    if (appointment.status === AppointmentStatus.CANCELLED) {
      throw new ConflictException('Appointment already cancelled');
    }

    // 3. Validate cancellation policy
    await this.validateCancellationPolicy(appointment);

    // 4. Update appointment
    const cancelledAppointment = await this.appointmentRepository.update(
      appointmentId,
      {
        status: AppointmentStatus.CANCELLED,
        cancellationReason: request.cancellationReason,
        cancelledBy: request.cancelledBy || context.userId,
        cancelledAt: new Date()
      }
    );

    // 5. Release time slot
    await this.availabilityService.releaseTimeSlot(
      appointment.providerId,
      appointment.scheduledDateTime,
      appointment.durationMinutes
    );

    // 6. Send notifications
    const patient = await this.patientService.getPatient(
      appointment.patientId,
      context
    );
    await this.notificationAdapter.sendAppointmentCancellation(
      patient.email,
      cancelledAppointment
    );

    // 7. Publish event
    await this.eventPublisher.publish(
      new AppointmentCancelledEvent(cancelledAppointment)
    );

    // 8. Audit log
    await this.auditLogger.log({
      eventType: 'APPOINTMENT_CANCELLED',
      resourceId: appointmentId,
      userId: context.userId,
      reason: request.cancellationReason
    });

    return cancelledAppointment;
  }

  // Additional methods...
}

// ============= DOMAIN MODELS =============

export class Appointment {
  id: string;
  patientId: string;
  providerId: string;
  appointmentType: AppointmentType;
  scheduledDateTime: Date;
  durationMinutes: number;
  status: AppointmentStatus;
  reason: string;
  notes?: string;
  cancellationReason?: string;
  cancelledBy?: string;
  cancelledAt?: Date;
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
  updatedBy?: string;
  version: number;

  constructor(data: Partial<Appointment>) {
    Object.assign(this, data);
  }

  isScheduled(): boolean {
    return this.status === AppointmentStatus.SCHEDULED;
  }

  isCancellable(): boolean {
    return [
      AppointmentStatus.SCHEDULED,
      AppointmentStatus.CONFIRMED
    ].includes(this.status);
  }

  getEndTime(): Date {
    return new Date(
      this.scheduledDateTime.getTime() + this.durationMinutes * 60000
    );
  }
}

export enum AppointmentStatus {
  SCHEDULED = 'SCHEDULED',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED',
  NO_SHOW = 'NO_SHOW'
}

export enum AppointmentType {
  CONSULTATION = 'CONSULTATION',
  FOLLOW_UP = 'FOLLOW_UP',
  PROCEDURE = 'PROCEDURE',
  EMERGENCY = 'EMERGENCY',
  TELEMEDICINE = 'TELEMEDICINE'
}
```

### 3.3 Medical Records Service

#### 3.3.1 Component Structure

```typescript
// ============= SERVICES =============

@Injectable()
export class MedicalRecordService {
  constructor(
    private readonly medicalRecordRepository: MedicalRecordRepository,
    private readonly patientService: PatientService,
    private readonly encryptionService: EncryptionService,
    private readonly fileStorageAdapter: FileStorageAdapter,
    private readonly eventPublisher: EventPublisher,
    private readonly auditLogger: AuditLogger,
    private readonly dataLineageService: DataLineageService
  ) {}

  async createMedicalRecord(
    request: CreateMedicalRecordRequest,
    context: RequestContext
  ): Promise<MedicalRecord> {
    // 1. Validate patient exists
    const patient = await this.patientService.getPatient(
      request.patientId,
      context
    );

    // 2. Check consent
    if (!patient.hasConsent(ConsentType.TREATMENT)) {
      throw new ForbiddenException('Patient has not provided treatment consent');
    }

    // 3. Encrypt sensitive content
    const encryptedContent = await this.encryptionService.encrypt(
      JSON.stringify(request.content)
    );

    // 4. Process attachments
    const attachments = await this.processAttachments(
      request.attachments,
      context
    );

    // 5. Create medical record
    const medicalRecord = new MedicalRecord({
      ...request,
      content: encryptedContent,
      attachments,
      createdBy: context.userId,
      createdAt: new Date()
    });

    // 6. Save to database
    const savedRecord = await this.medicalRecordRepository.save(medicalRecord);

    // 7. Create data lineage
    await this.dataLineageService.createLineage({
      recordId: savedRecord.id,
      sourceSystem: 'EHR-SYSTEM-V2',
      createdBy: context.userId,
      createdAt: new Date()
    });

    // 8. Publish event
    await this.eventPublisher.publish(
      new MedicalRecordCreatedEvent(savedRecord)
    );

    // 9. Audit log
    await this.auditLogger.log({
      eventType: 'MEDICAL_RECORD_CREATED',
      resourceId: savedRecord.id,
      patientId: request.patientId,
      userId: context.userId,
      dataClassification: 'PHI',
      severity: 'HIGH'
    });

    return savedRecord;
  }

  async getMedicalRecord(
    recordId: string,
    context: RequestContext
  ): Promise<MedicalRecord> {
    // 1. Fetch record
    const record = await this.medicalRecordRepository.findById(recordId);
    if (!record) {
      throw new NotFoundException('Medical record not found');
    }

    // 2. Check access permissions (ABAC)
    await this.checkAccessPermissions(record, context);

    // 3. Check break-glass scenario
    if (context.breakGlass) {
      await this.handleBreakGlassAccess(record, context);
    }

    // 4. Decrypt content
    const decryptedContent = await this.encryptionService.decrypt(
      record.content
    );
    record.content = JSON.parse(decryptedContent);

    // 5. Generate signed URLs for attachments
    record.attachments = await Promise.all(
      record.attachments.map(att => this.generateSignedUrl(att))
    );

    // 6. Update data lineage
    await this.dataLineageService.recordAccess({
      recordId,
      userId: context.userId,
      accessTime: new Date(),
      accessType: 'READ',
      ipAddress: context.ipAddress
    });

    // 7. Audit log
    await this.auditLogger.log({
      eventType: 'MEDICAL_RECORD_ACCESSED',
      resourceId: recordId,
      patientId: record.patientId,
      userId: context.userId,
      dataClassification: 'PHI',
      severity: 'HIGH',
      breakGlass: context.breakGlass || false
    });

    return record;
  }

  async getPatientMedicalHistory(
    patientId: string,
    filters: MedicalHistoryFilters,
    context: RequestContext
  ): Promise<PaginatedResult<MedicalRecord>> {
    // 1. Validate patient access
    const patient = await this.patientService.getPatient(patientId, context);

    // 2. Build query
    const query = this.buildHistoryQuery(patientId, filters);

    // 3. Execute query
    const results = await this.medicalRecordRepository.search(
      query,
      filters.page,
      filters.pageSize
    );

    // 4. Decrypt records
    const decryptedRecords = await Promise.all(
      results.data.map(async record => {
        const decryptedContent = await this.encryptionService.decrypt(
          record.content
        );
        return {
          ...record,
          content: JSON.parse(decryptedContent)
        };
      })
    );

    // 5. Audit log
    await this.auditLogger.log({
      eventType: 'MEDICAL_HISTORY_ACCESSED',
      patientId,
      userId: context.userId,
      recordCount: results.totalItems,
      filters
    });

    return {
      data: decryptedRecords,
      pagination: results.pagination
    };
  }

  private async processAttachments(
    attachments: AttachmentUpload[],
    context: RequestContext
  ): Promise<Attachment[]> {
    return Promise.all(
      attachments.map(async att => {
        // Upload to secure storage
        const url = await this.fileStorageAdapter.upload(
          att.file,
          {
            encryption: 'AES-256',
            folder: 'medical-records',
            metadata: {
              uploadedBy: context.userId,
              uploadedAt: new Date().toISOString()
            }
          }
        );

        return {
          id: generateId(),
          fileName: att.fileName,
          fileType: att.fileType,
          fileSize: att.fileSize,
          url,
          uploadedAt: new Date()
        };
      })
    );
  }

  private async generateSignedUrl(attachment: Attachment): Promise<Attachment> {
    const signedUrl = await this.fileStorageAdapter.generateSignedUrl(
      attachment.url,
      { expiresIn: 3600 } // 1 hour
    );
    return {
      ...attachment,
      url: signedUrl
    };
  }

  private async checkAccessPermissions(
    record: MedicalRecord,
    context: RequestContext
  ): Promise<void> {
    // ABAC policy evaluation
    const policy = {
      subject: context,
      resource: record,
      action: 'READ'
    };

    const allowed = await this.abacService.evaluate(policy);
    if (!allowed) {
      throw new ForbiddenException('Access denied to medical record');
    }
  }

  private async handleBreakGlassAccess(
    record: MedicalRecord,
    context: RequestContext
  ): Promise<void> {
    // Log critical audit event
    await this.auditLogger.log({
      eventType: 'BREAK_GLASS_ACCESS',
      resourceId: record.id,
      patientId: record.patientId,
      userId: context.userId,
      reason: context.breakGlassReason,
      severity: 'CRITICAL'
    });

    // Send alert to security team
    await this.notificationAdapter.sendSecurityAlert({
      type: 'BREAK_GLASS_ACCESS',
      userId: context.userId,
      resourceId: record.id,
      timestamp: new Date()
    });
  }
}

// ============= DOMAIN MODELS =============

export class MedicalRecord {
  id: string;
  patientId: string;
  providerId: string;
  appointmentId?: string;
  recordType: MedicalRecordType;
  recordDate: Date;
  content: any; // Encrypted JSON
  attachments: Attachment[];
  notes?: string;
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
  updatedBy?: string;
  version: number;
  dataLineage: DataLineage;

  constructor(data: Partial<MedicalRecord>) {
    Object.assign(this, data);
  }
}

export enum MedicalRecordType {
  DIAGNOSIS = 'DIAGNOSIS',
  PRESCRIPTION = 'PRESCRIPTION',
  LAB_RESULT = 'LAB_RESULT',
  CLINICAL_NOTE = 'CLINICAL_NOTE',
  PROCEDURE = 'PROCEDURE',
  IMAGING = 'IMAGING'
}

export class Attachment {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  url: string;
  uploadedAt: Date;
}

export class DataLineage {
  sourceSystem: string;
  createdBy: string;
  modifiedBy?: string;
  accessHistory: AccessRecord[];
}

export class AccessRecord {
  userId: string;
  accessTime: Date;
  accessType: 'READ' | 'WRITE' | 'DELETE';
  ipAddress: string;
}
```

---

## 4. DATA ARCHITECTURE

### 4.1 Database Schema Design

#### 4.1.1 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DATABASE SCHEMA                              │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────────┐
│      patients        │
├──────────────────────┤
│ id (PK)              │ VARCHAR(50)
│ mrn (UNIQUE)         │ VARCHAR(50)
│ first_name           │ VARCHAR(50)
│ middle_name          │ VARCHAR(50)
│ last_name            │ VARCHAR(50)
│ date_of_birth        │ DATE
│ gender               │ CHAR(1)
│ ssn_encrypted        │ TEXT
│ email                │ VARCHAR(100)
│ phone                │ VARCHAR(20)
│ status               │ VARCHAR(20)
│ created_at           │ TIMESTAMP
│ updated_at           │ TIMESTAMP
│ created_by           │ VARCHAR(50)
│ updated_by           │ VARCHAR(50)
│ deleted_at           │ TIMESTAMP
│ deleted_by           │ VARCHAR(50)
│ version              │ INTEGER
└──────────┬───────────┘
           │
           │ 1:1
           ▼
┌──────────────────────┐
│  patient_addresses   │
├──────────────────────┤
│ id (PK)              │
│ patient_id (FK)      │───┐
│ street               │   │
│ street2              │   │
│ city                 │   │
│ state                │   │
│ zip_code             │   │
│ country              │   │
│ created_at           │   │
│ updated_at           │   │
└──────────────────────┘   │
                           │
           ┌───────────────┘
           │
           │ 1:1
           ▼
┌──────────────────────┐
│ emergency_contacts   │
├──────────────────────┤
│ id (PK)              │
│ patient_id (FK)      │
│ name                 │
│ relationship         │
│ phone                │
│ email                │
│ created_at           │
│ updated_at           │
└──────────────────────┘

┌──────────────────────┐
│  insurance_info      │
├──────────────────────┤
│ id (PK)              │
│ patient_id (FK)      │───┐
│ provider             │   │
│ policy_number_enc    │   │
│ group_number         │   │
│ effective_date       │   │
│ expiration_date      │   │
│ created_at           │   │
│ updated_at           │   │
└──────────────────────┘   │
                           │
           ┌───────────────┘
           │
           │ 1:N
           ▼
┌──────────────────────┐
│  patient_consents    │
├──────────────────────┤
│ id (PK)              │
│ patient_id (FK)      │
│ treatment_consent    │ BOOLEAN
│ data_processing      │ BOOLEAN
│ marketing_consent    │ BOOLEAN
│ research_consent     │ BOOLEAN
│ consent_date         │ TIMESTAMP
│ consent_version      │ VARCHAR(10)
│ ip_address           │ VARCHAR(45)
│ created_at           │ TIMESTAMP
│ updated_at           │ TIMESTAMP
└──────────────────────┘

┌──────────────────────┐
│    appointments      │
├──────────────────────┤
│ id (PK)              │
│ patient_id (FK)      │───┐
│ provider_id          │   │
│ appointment_type     │   │
│ scheduled_datetime   │   │
│ duration_minutes     │   │
│ status               │   │
│ reason               │   │
│ notes                │   │
│ cancellation_reason  │   │
│ cancelled_by         │   │
│ cancelled_at         │   │
│ created_at           │   │
│ updated_at           │   │
│ created_by           │   │
│ updated_by           │   │
│ version              │   │
└──────────┬───────────┘   │
           │               │
           │ 1:N           │
           ▼               │
┌──────────────────────┐   │
│   medical_records    │   │
├──────────────────────┤   │
│ id (PK)              │   │
│ patient_id (FK)      │◄──┘
│ provider_id          │
│ appointment_id (FK)  │───┐
│ record_type          │   │
│ record_date          │   │
│ content_encrypted    │   │
│ notes                │   │
│ created_at           │   │
│ updated_at           │   │
│ created_by           │   │
│ updated_by           │   │
│ version              │   │
└──────────┬───────────┘   │
           │               │
           │ 1:N           │
           ▼               │
┌──────────────────────┐   │
│     attachments      │   │
├──────────────────────┤   │
│ id (PK)              │   │
│ record_id (FK)       │◄──┘
│ file_name            │
│ file_type            │
│ file_size            │
│ storage_url          │
│ uploaded_at          │
└──────────────────────┘

┌──────────────────────┐
│    data_lineage      │
├──────────────────────┤
│ id (PK)              │
│ record_id (FK)       │───┐
│ source_system        │   │
│ created_by           │   │
│ modified_by          │   │
│ created_at           │   │
│ updated_at           │   │
└──────────────────────┘   │
                           │
           ┌───────────────┘
           │
           │ 1:N
           ▼
┌──────────────────────┐
│   access_history     │
├──────────────────────┤
│ id (PK)              │
│ lineage_id (FK)      │
│ user_id              │
│ access_time          │
│ access_type          │
│ ip_address           │
└──────────────────────┘

┌──────────────────────┐
│     audit_logs       │
├──────────────────────┤
│ id (PK)              │
│ event_id (UNIQUE)    │
│ timestamp            │
│ event_type           │
│ severity             │
│ user_id              │
│ resource_type        │
│ resource_id          │
│ action               │
│ ip_address           │
│ user_agent           │
│ request_id           │
│ correlation_id       │
│ data_classification  │
│ details (JSONB)      │
└──────────────────────┘
```

#### 4.1.2 Table Definitions

```sql
-- ============= PATIENTS TABLE =============

CREATE TABLE patients (
    id VARCHAR(50) PRIMARY KEY,
    mrn VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F', 'O', 'U')),
    ssn_encrypted TEXT NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DECEASED', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    version INTEGER NOT NULL DEFAULT 1
);

-- Indexes
CREATE INDEX idx_patients_email ON patients(email) WHERE status != 'DELETED';
CREATE INDEX idx_patients_mrn ON patients(mrn);
CREATE INDEX idx_patients_name ON patients(last_name, first_name);
CREATE INDEX idx_patients_dob ON patients(date_of_birth);
CREATE INDEX idx_patients_status ON patients(status);
CREATE INDEX idx_patients_created_at ON patients(created_at);

-- ============= PATIENT ADDRESSES TABLE =============

CREATE TABLE patient_addresses (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    street VARCHAR(100) NOT NULL,
    street2 VARCHAR(100),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    country VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_patient_addresses_patient ON patient_addresses(patient_id);

-- ============= EMERGENCY CONTACTS TABLE =============

CREATE TABLE emergency_contacts (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_emergency_contacts_patient ON emergency_contacts(patient_id);

-- ============= INSURANCE INFO TABLE =============

CREATE TABLE insurance_info (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    provider VARCHAR(100) NOT NULL,
    policy_number_encrypted TEXT NOT NULL,
    group_number VARCHAR(50),
    effective_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_insurance_info_patient ON insurance_info(patient_id);

-- ============= PATIENT CONSENTS TABLE =============

CREATE TABLE patient_consents (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    treatment_consent BOOLEAN NOT NULL DEFAULT false,
    data_processing_consent BOOLEAN NOT NULL DEFAULT false,
    marketing_consent BOOLEAN NOT NULL DEFAULT false,
    research_consent BOOLEAN NOT NULL DEFAULT false,
    consent_date TIMESTAMP NOT NULL,
    consent_version VARCHAR(10) NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patient_consents_patient ON patient_consents(patient_id);
CREATE INDEX idx_patient_consents_date ON patient_consents(consent_date);

-- ============= APPOINTMENTS TABLE =============

CREATE TABLE appointments (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id),
    provider_id VARCHAR(50) NOT NULL,
    appointment_type VARCHAR(20) NOT NULL 
        CHECK (appointment_type IN ('CONSULTATION', 'FOLLOW_UP', 'PROCEDURE', 'EMERGENCY', 'TELEMEDICINE')),
    scheduled_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    reason TEXT,
    notes TEXT,
    cancellation_reason TEXT,
    cancelled_by VARCHAR(50),
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50),
    version INTEGER NOT NULL DEFAULT 1
);

-- Indexes
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_provider ON appointments(provider_id);
CREATE INDEX idx_appointments_datetime ON appointments(scheduled_datetime);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_created_at ON appointments(created_at);

-- ============= MEDICAL RECORDS TABLE =============

CREATE TABLE medical_records (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id),
    provider_id VARCHAR(50) NOT NULL,
    appointment_id VARCHAR(50) REFERENCES appointments(id),
    record_type VARCHAR(20) NOT NULL
        CHECK (record_type IN ('DIAGNOSIS', 'PRESCRIPTION', 'LAB_RESULT', 'CLINICAL_NOTE', 'PROCEDURE', 'IMAGING')),
    record_date TIMESTAMP NOT NULL,
    content_encrypted TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50),
    version INTEGER NOT NULL DEFAULT 1
);

-- Indexes
CREATE INDEX idx_medical_records_patient ON medical_records(patient_id);
CREATE INDEX idx_medical_records_provider ON medical_records(provider_id);
CREATE INDEX idx_medical_records_appointment ON medical_records(appointment_id);
CREATE INDEX idx_medical_records_type ON medical_records(record_type);
CREATE INDEX idx_medical_records_date ON medical_records(record_date);
CREATE INDEX idx_medical_records_created_at ON medical_records(created_at);

-- ============= ATTACHMENTS TABLE =============

CREATE TABLE attachments (
    id VARCHAR(50) PRIMARY KEY,
    record_id VARCHAR(50) NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_url TEXT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attachments_record ON attachments(record_id);

-- ============= DATA LINEAGE TABLE =============

CREATE TABLE data_lineage (
    id VARCHAR(50) PRIMARY KEY,
    record_id VARCHAR(50) NOT NULL REFERENCES medical_records(id) ON DELETE CASCADE,
    source_system VARCHAR(100) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    modified_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_data_lineage_record ON data_lineage(record_id);

-- ============= ACCESS HISTORY TABLE =============

CREATE TABLE access_history (
    id VARCHAR(50) PRIMARY KEY,
    lineage_id VARCHAR(50) NOT NULL REFERENCES data_lineage(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL,
    access_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    access_type VARCHAR(10) NOT NULL CHECK (access_type IN ('READ', 'WRITE', 'DELETE')),
    ip_address VARCHAR(45) NOT NULL
);

CREATE INDEX idx_access_history_lineage ON access_history(lineage_id);
CREATE INDEX idx_access_history_user ON access_history(user_id);
CREATE INDEX idx_access_history_time ON access_history(access_time);

-- ============= AUDIT LOGS TABLE =============

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) UNIQUE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'WARN', 'ERROR', 'CRITICAL')),
    user_id VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id VARCHAR(50),
    action VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(50),
    correlation_id VARCHAR(50),
    data_classification VARCHAR(20),
    details JSONB
);

-- Indexes
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_severity ON audit_logs(severity);
CREATE INDEX idx_audit_logs_request ON audit_logs(request_id);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_details ON audit_logs USING GIN (details);
```

### 4.2 Data Access Patterns

#### 4.2.1 Read Patterns

```sql
-- Pattern 1: Get Patient by ID (with related data)
SELECT 
    p.*,
    pa.*,
    ec.*,
    ii.*,
    pc.*
FROM patients p
LEFT JOIN patient_addresses pa ON p.id = pa.patient_id
LEFT JOIN emergency_contacts ec ON p.id = ec.patient_id
LEFT JOIN insurance_info ii ON p.id = ii.patient_id
LEFT JOIN patient_consents pc ON p.id = pc.patient_id
WHERE p.id = $1 AND p.status != 'DELETED';

-- Pattern 2: Search Patients (with pagination)
SELECT 
    p.*,
    COUNT(*) OVER() as total_count
FROM patients p
WHERE 
    p.status != 'DELETED'
    AND ($1::VARCHAR IS NULL OR p.first_name ILIKE '%' || $1 || '%')
    AND ($2::VARCHAR IS NULL OR p.last_name ILIKE '%' || $2 || '%')
    AND ($3::DATE IS NULL OR p.date_of_birth = $3)
    AND ($4::VARCHAR IS NULL OR p.email = $4)
ORDER BY p.created_at DESC
LIMIT $5 OFFSET $6;

-- Pattern 3: Get Appointments for Patient
SELECT 
    a.*
FROM appointments a
WHERE 
    a.patient_id = $1
    AND a.scheduled_datetime BETWEEN $2 AND $3
    AND ($4::VARCHAR IS NULL OR a.status = $4)
ORDER BY a.scheduled_datetime ASC;

-- Pattern 4: Get Medical History
SELECT 
    mr.*,
    array_agg(att.*) as attachments
FROM medical_records mr
LEFT JOIN attachments att ON mr.id = att.record_id
WHERE 
    mr.patient_id = $1
    AND ($2::VARCHAR IS NULL OR mr.record_type = $2)
    AND mr.record_date BETWEEN $3 AND $4
GROUP BY mr.id
ORDER BY mr.record_date DESC
LIMIT $5 OFFSET $6;
```

#### 4.2.2 Write Patterns

```sql
-- Pattern 1: Create Patient (with transaction)
BEGIN;

INSERT INTO patients (
    id, mrn, first_name, middle_name, last_name, date_of_birth,
    gender, ssn_encrypted, email, phone, status, created_by
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'ACTIVE', $11
);

INSERT INTO patient_addresses (
    id, patient_id, street, street2, city, state, zip_code, country
) VALUES (
    $12, $1, $13, $14, $15, $16, $17, $18
);

INSERT INTO emergency_contacts (
    id, patient_id, name, relationship, phone, email
) VALUES (
    $19, $1, $20, $21, $22, $23
);

INSERT INTO patient_consents (
    id, patient_id, treatment_consent, data_processing_consent,
    marketing_consent, research_consent, consent_date, consent_version, ip_address
) VALUES (
    $24, $1, $25, $26, $27, $28, $29, $30, $31
);

COMMIT;

-- Pattern 2: Update Patient (with optimistic locking)
UPDATE patients
SET 
    first_name = $2,
    last_name = $3,
    email = $4,
    phone = $5,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = $6,
    version = version + 1
WHERE 
    id = $1 
    AND version = $7
    AND status != 'DELETED'
RETURNING *;

-- Pattern 3: Soft Delete Patient
UPDATE patients
SET 
    status = 'DELETED',
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = $2,
    updated_at = CURRENT_TIMESTAMP
WHERE 
    id = $1
    AND status != 'DELETED';
```

### 4.3 Caching Strategy

#### 4.3.1 Cache Layers

```yaml
L1 Cache (Application Memory):
  Technology: In-memory Map
  TTL: 60 seconds
  Size: 1000 entries
  Use Cases:
    - Frequently accessed configuration
    - User session data
    - JWT token validation cache

L2 Cache (Distributed Cache):
  Technology: Redis
  TTL: 300 seconds (5 minutes)
  Eviction Policy: LRU
  Use Cases:
    - Patient records
    - Appointment data
    - Provider availability
    - Search results

Cache Keys:
  Patient: "patient:{patientId}"
  Appointment: "appointment:{appointmentId}"
  Search: "search:patients:{hash(query)}"
  Availability: "availability:{providerId}:{date}"

Cache Invalidation:
  Write-Through:
    - Update cache on write
    - Ensure consistency
  
  Time-Based:
    - Automatic expiration via TTL
    - Suitable for read-heavy data
  
  Event-Based:
    - Invalidate on domain events
    - PatientUpdated → invalidate patient:{id}
    - AppointmentCancelled → invalidate appointment:{id}
```

#### 4.3.2 Cache Implementation

```typescript
@Injectable()
export class CacheManager {
  constructor(
    private readonly redis: Redis,
    private readonly logger: Logger
  ) {}

  async get<T>(key: string): Promise<T | null> {
    try {
      const cached = await this.redis.get(key);
      if (cached) {
        this.logger.debug(`Cache hit: ${key}`);
        return JSON.parse(cached);
      }
      this.logger.debug(`Cache miss: ${key}`);
      return null;
    } catch (error) {
      this.logger.error(`Cache get error: ${error.message}`);
      return null; // Fail gracefully
    }
  }

  async set<T>(
    key: string,
    value: T,
    ttl: number = 300
  ): Promise<void> {
    try {
      await this.redis.setex(
        key,
        ttl,
        JSON.stringify(value)
      );
      this.logger.debug(`Cache set: ${key}, TTL: ${ttl}s`);
    } catch (error) {
      this.logger.error(`Cache set error: ${error.message}`);
      // Don't throw - caching is not critical
    }
  }

  async delete(key: string): Promise<void> {
    try {
      await this.redis.del(key);
      this.logger.debug(`Cache deleted: ${key}`);
    } catch (error) {
      this.logger.error(`Cache delete error: ${error.message}`);
    }
  }

  async deletePattern(pattern: string): Promise<void> {
    try {
      const keys = await this.redis.keys(pattern);
      if (keys.length > 0) {
        await this.redis.del(...keys);
        this.logger.debug(`Cache deleted pattern: ${pattern}, count: ${keys.length}`);
      }
    } catch (error) {
      this.logger.error(`Cache delete pattern error: ${error.message}`);
    }
  }
}
```

---

## 5. API INTERFACE SPECIFICATIONS

### 5.1 Request/Response DTOs

```typescript
// ============= REQUEST DTOs =============

export class CreatePatientRequest {
  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  @Matches(/^[a-zA-Z\s\-']+$/)
  firstName: string;

  @IsString()
  @IsOptional()
  @Length(1, 50)
  @Matches(/^[a-zA-Z\s\-']+$/)
  middleName?: string;

  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  @Matches(/^[a-zA-Z\s\-']+$/)
  lastName: string;

  @IsDateString()
  @IsNotEmpty()
  dateOfBirth: string;

  @IsEnum(Gender)
  @IsNotEmpty()
  gender: Gender;

  @IsString()
  @IsNotEmpty()
  @Matches(/^\d{3}-\d{2}-\d{4}$/)
  ssn: string;

  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsString()
  @IsNotEmpty()
  @Matches(/^\+?[1-9]\d{1,14}$/)
  phone: string;

  @ValidateNested()
  @Type(() => AddressDto)
  @IsNotEmpty()
  address: AddressDto;

  @ValidateNested()
  @Type(() => EmergencyContactDto)
  @IsNotEmpty()
  emergencyContact: EmergencyContactDto;

  @ValidateNested()
  @Type(() => ConsentDataDto)
  @IsNotEmpty()
  consent: ConsentDataDto;

  @ValidateNested()
  @Type(() => InsuranceInfoDto)
  @IsOptional()
  insuranceInfo?: InsuranceInfoDto;
}

export class UpdatePatientRequest {
  @IsString()
  @IsOptional()
  @Length(1, 50)
  firstName?: string;

  @IsString()
  @IsOptional()
  @Length(1, 50)
  lastName?: string;

  @IsEmail()
  @IsOptional()
  email?: string;

  @IsString()
  @IsOptional()
  @Matches(/^\+?[1-9]\d{1,14}$/)
  phone?: string;

  @ValidateNested()
  @Type(() => AddressDto)
  @IsOptional()
  address?: AddressDto;

  @ValidateNested()
  @Type(() => EmergencyContactDto)
  @IsOptional()
  emergencyContact?: EmergencyContactDto;

  @ValidateNested()
  @Type(() => InsuranceInfoDto)
  @IsOptional()
  insuranceInfo?: InsuranceInfoDto;
}

export class PatientSearchQuery {
  @IsString()
  @IsOptional()
  @Length(2, 50)
  firstName?: string;

  @IsString()
  @IsOptional()
  @Length(2, 50)
  lastName?: string;

  @IsDateString()
  @IsOptional()
  dateOfBirth?: string;

  @IsEmail()
  @IsOptional()
  email?: string;

  @IsString()
  @IsOptional()
  phone?: string;

  @IsString()
  @IsOptional()
  mrn?: string;

  @IsInt()
  @Min(1)
  @IsOptional()
  @Type(() => Number)
  page?: number = 1;

  @IsInt()
  @Min(1)
  @Max(100)
  @IsOptional()
  @Type(() => Number)
  pageSize?: number = 20;

  @IsString()
  @IsOptional()
  sortBy?: string = 'createdAt';

  @IsEnum(['asc', 'desc'])
  @IsOptional()
  sortOrder?: 'asc' | 'desc' = 'desc';
}

// ============= RESPONSE DTOs =============

export class PatientResponse {
  id: string;
  mrn: string;
  firstName: string;
  middleName?: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  ssn: string; // Masked
  email: string;
  phone: string;
  address: AddressDto;
  emergencyContact: EmergencyContactDto;
  insuranceInfo?: InsuranceInfoDto;
  status: PatientStatus;
  consent: ConsentDataDto;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export class PatientSearchResponse {
  data: PatientResponse[];
  pagination: PaginationMetadata;
}

export class PaginationMetadata {
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
  hasNext: boolean;
  hasPrevious: boolean;
  links?: {
    self: string;
    first: string;
    last: string;
    next?: string;
    previous?: string;
  };
}

export class ErrorResponse {
  error: string;
  message: string;
  details?: Array<{
    field: string;
    message: string;
    code: string;
  }>;
  timestamp: string;
  path: string;
  requestId: string;
  traceId?: string;
}

// ============= NESTED DTOs =============

export class AddressDto {
  @IsString()
  @IsNotEmpty()
  @Length(1, 100)
  street: string;

  @IsString()
  @IsOptional()
  @Length(1, 100)
  street2?: string;

  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  city: string;

  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  state: string;

  @IsString()
  @IsNotEmpty()
  @Matches(/^\d{5}(-\d{4})?$/)
  zipCode: string;

  @IsString()
  @IsNotEmpty()
  @Length(2, 3)
  @Matches(/^[A-Z]{2,3}$/)
  country: string;
}

export class EmergencyContactDto {
  @IsString()
  @IsNotEmpty()
  @Length(1, 100)
  name: string;

  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  relationship: string;

  @IsString()
  @IsNotEmpty()
  @Matches(/^\+?[1-9]\d{1,14}$/)
  phone: string;

  @IsEmail()
  @IsOptional()
  email?: string;
}

export class ConsentDataDto {
  @IsBoolean()
  @IsNotEmpty()
  treatmentConsent: boolean;

  @IsBoolean()
  @IsNotEmpty()
  dataProcessingConsent: boolean;

  @IsBoolean()
  @IsOptional()
  marketingConsent?: boolean;

  @IsBoolean()
  @IsOptional()
  researchConsent?: boolean;

  @IsDateString()
  @IsNotEmpty()
  consentDate: string;

  @IsString()
  @IsOptional()
  consentVersion?: string;

  @IsString()
  @IsOptional()
  ipAddress?: string;
}

export class InsuranceInfoDto {
  @IsString()
  @IsNotEmpty()
  @Length(1, 100)
  provider: string;

  @IsString()
  @IsNotEmpty()
  @Length(1, 50)
  policyNumber: string;

  @IsString()
  @IsOptional()
  @Length(1, 50)
  groupNumber?: string;

  @IsDateString()
  @IsNotEmpty()
  effectiveDate: string;

  @IsDateString()
  @IsNotEmpty()
  expirationDate: string;
}
```

### 5.2 Validation Rules

```typescript
// ============= CUSTOM VALIDATORS =============

@ValidatorConstraint({ name: 'isAdult', async: false })
export class IsAdultConstraint implements ValidatorConstraintInterface {
  validate(dateOfBirth: string) {
    const dob = new Date(dateOfBirth);
    const today = new Date();
    const age = today.getFullYear() - dob.getFullYear();
    return age >= 18;
  }

  defaultMessage() {
    return 'Patient must be at least 18 years old';
  }
}

export function IsAdult(validationOptions?: ValidationOptions) {
  return function (object: Object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName,
      options: validationOptions,
      constraints: [],
      validator: IsAdultConstraint,
    });
  };
}

@ValidatorConstraint({ name: 'isFutureDate', async: false })
export class IsFutureDateConstraint implements ValidatorConstraintInterface {
  validate(date: string) {
    const inputDate = new Date(date);
    const today = new Date();
    return inputDate > today;
  }

  defaultMessage() {
    return 'Date must be in the future';
  }
}

export function IsFutureDate(validationOptions?: ValidationOptions) {
  return function (object: Object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName,
      options: validationOptions,
      constraints: [],
      validator: IsFutureDateConstraint,
    });
  };
}

// ============= VALIDATION PIPE =============

@Injectable()
export class ValidationPipe implements PipeTransform {
  async transform(value: any, metadata: ArgumentMetadata) {
    if (!metadata.metatype || !this.toValidate(metadata.metatype)) {
      return value;
    }

    const object = plainToClass(metadata.metatype, value);
    const errors = await validate(object, {
      whitelist: true,
      forbidNonWhitelisted: true,
      forbidUnknownValues: true,
    });

    if (errors.length > 0) {
      throw new BadRequestException({
        error: 'VALIDATION_ERROR',
        message: 'Validation failed',
        details: this.formatErrors(errors),
      });
    }

    return object;
  }

  private toValidate(metatype: Function): boolean {
    const types: Function[] = [String, Boolean, Number, Array, Object];
    return !types.includes(metatype);
  }

  private formatErrors(errors: ValidationError[]) {
    return errors.map(error => ({
      field: error.property,
      message: Object.values(error.constraints || {})[0],
      code: Object.keys(error.constraints || {})[0].toUpperCase(),
    }));
  }
}
```

### 5.3 Error Handling

```typescript
// ============= CUSTOM EXCEPTIONS =============

export class NotFoundException extends HttpException {
  constructor(message: string) {
    super(
      {
        error: 'NOT_FOUND',
        message,
        timestamp: new Date().toISOString(),
      },
      HttpStatus.NOT_FOUND
    );
  }
}

export class ConflictException extends HttpException {
  constructor(message: string) {
    super(
      {
        error: 'CONFLICT',
        message,
        timestamp: new Date().toISOString(),
      },
      HttpStatus.CONFLICT
    );
  }
}

export class ForbiddenException extends HttpException {
  constructor(message: string) {
    super(
      {
        error: 'FORBIDDEN',
        message,
        timestamp: new Date().toISOString(),
      },
      HttpStatus.FORBIDDEN
    );
  }
}

export class PreconditionFailedException extends HttpException {
  constructor(message: string) {
    super(
      {
        error: 'PRECONDITION_FAILED',
        message,
        timestamp: new Date().toISOString(),
      },
      HttpStatus.PRECONDITION_FAILED
    );
  }
}

// ============= GLOBAL EXCEPTION FILTER =============

@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  constructor(
    private readonly logger: Logger,
    private readonly auditLogger: AuditLogger
  ) {}

  catch(exception: any, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    const status = exception instanceof HttpException
      ? exception.getStatus()
      : HttpStatus.INTERNAL_SERVER_ERROR;

    const errorResponse: ErrorResponse = {
      error: this.getErrorCode(exception),
      message: this.getErrorMessage(exception),
      timestamp: new Date().toISOString(),
      path: request.url,
      requestId: request.headers['x-request-id'] as string,
      traceId: request.headers['x-trace-id'] as string,
    };

    // Add validation details if available
    if (exception.response?.details) {
      errorResponse.details = exception.response.details;
    }

    // Log error
    this.logger.error(
      `${request.method} ${request.url} - ${status} - ${errorResponse.message}`,
      exception.stack
    );

    // Audit log for security-related errors
    if (status === HttpStatus.UNAUTHORIZED || status === HttpStatus.FORBIDDEN) {
      this.auditLogger.log({
        eventType: 'SECURITY_ERROR',
        severity: 'WARN',
        userId: request.user?.id,
        ipAddress: request.ip,
        path: request.url,
        error: errorResponse.error,
        message: errorResponse.message,
      });
    }

    response.status(status).json(errorResponse);
  }

  private getErrorCode(exception: any): string {
    if (exception instanceof HttpException) {
      const response = exception.getResponse();
      return typeof response === 'object' && response['error']
        ? response['error']
        : exception.name;
    }
    return 'INTERNAL_SERVER_ERROR';
  }

  private getErrorMessage(exception: any): string {
    if (exception instanceof HttpException) {
      const response = exception.getResponse();
      return typeof response === 'object' && response['message']
        ? response['message']
        : exception.message;
    }
    return 'An unexpected error occurred';
  }
}
```

---

## 6. SECURITY ARCHITECTURE

### 6.1 Authentication Implementation

```typescript
// ============= AUTH GUARD =============

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private readonly jwtService: JwtService,
    private readonly cacheManager: CacheManager,
    private readonly auditLogger: AuditLogger
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const token = this.extractToken(request);

    if (!token) {
      throw new UnauthorizedException('No authentication token provided');
    }

    try {
      // Check token blacklist
      const isBlacklisted = await this.cacheManager.get(`blacklist:${token}`);
      if (isBlacklisted) {
        throw new UnauthorizedException('Token has been revoked');
      }

      // Verify JWT
      const payload = await this.jwtService.verifyAsync(token, {
        secret: process.env.JWT_SECRET,
      });

      // Check token expiration
      if (payload.exp && payload.exp < Date.now() / 1000) {
        throw new UnauthorizedException('Token has expired');
      }

      // Attach user to request
      request.user = {
        id: payload.sub,
        email: payload.email,
        roles: payload.roles || [],
        scopes: payload.scopes || [],
        attributes: payload.attributes || {},
      };

      return true;
    } catch (error) {
      // Audit failed authentication
      await this.auditLogger.log({
        eventType: 'AUTHENTICATION_FAILED',
        severity: 'WARN',
        ipAddress: request.ip,
        userAgent: request.headers['user-agent'],
        error: error.message,
      });

      throw new UnauthorizedException('Invalid authentication token');
    }
  }

  private extractToken(request: Request): string | null {
    const authHeader = request.headers.authorization;
    if (!authHeader) {
      return null;
    }

    const [type, token] = authHeader.split(' ');
    return type === 'Bearer' ? token : null;
  }
}

// ============= RBAC GUARD =============

@Injectable()
export class RBACGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly auditLogger: AuditLogger
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const requiredPermissions = this.reflector.get<string[]>(
      'permissions',
      context.getHandler()
    );

    if (!requiredPermissions || requiredPermissions.length === 0) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const user = request.user;

    if (!user) {
      throw new UnauthorizedException('User not authenticated');
    }

    const hasPermission = requiredPermissions.every(permission =>
      user.scopes.includes(permission)
    );

    if (!hasPermission) {
      // Audit authorization failure
      await this.auditLogger.log({
        eventType: 'AUTHORIZATION_FAILED',
        severity: 'WARN',
        userId: user.id,
        requiredPermissions,
        userPermissions: user.scopes,
        path: request.url,
      });

      throw new ForbiddenException('Insufficient permissions');
    }

    return true;
  }
}

// ============= ABAC SERVICE =============

@Injectable()
export class ABACService {
  async evaluate(policy: ABACPolicy): Promise<boolean> {
    const { subject, resource, action } = policy;

    // Rule 1: Admin has access to everything
    if (subject.roles.includes('admin')) {
      return true;
    }

    // Rule 2: Physicians can only access their own patients
    if (subject.roles.includes('physician')) {
      if (resource.type === 'patient') {
        return resource.assignedPhysician === subject.id;
      }
    }

    // Rule 3: Department-based access
    if (subject.attributes.department) {
      if (resource.department === subject.attributes.department) {
        return true;
      }
    }

    // Rule 4: Patient can access their own data
    if (subject.roles.includes('patient')) {
      if (resource.type === 'patient' && resource.id === subject.id) {
        return action === 'READ';
      }
    }

    return false;
  }
}

interface ABACPolicy {
  subject: {
    id: string;
    roles: string[];
    attributes: Record<string, any>;
  };
  resource: {
    type: string;
    id: string;
    [key: string]: any;
  };
  action: string;
}

// ============= DECORATORS =============

export const RequirePermission = (...permissions: string[]) =>
  SetMetadata('permissions', permissions);

export const RequireRole = (...roles: string[]) =>
  SetMetadata('roles', roles);

export const CurrentUser = createParamDecorator(
  (data: unknown, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    return request.user;
  },
);
```

### 6.2 Encryption Service

```typescript
@Injectable()
export class EncryptionService {
  private readonly algorithm = 'aes-256-gcm';
  private readonly keyLength = 32;
  private readonly ivLength = 16;
  private readonly tagLength = 16;

  constructor(
    private readonly kmsClient: KMSClient,
    private readonly logger: Logger
  ) {}

  async encrypt(plaintext: string): Promise<string> {
    try {
      // Get encryption key from KMS
      const dataKey = await this.getDataKey();

      // Generate IV
      const iv = crypto.randomBytes(this.ivLength);

      // Create cipher
      const cipher = crypto.createCipheriv(
        this.algorithm,
        dataKey,
        iv
      );

      // Encrypt
      let encrypted = cipher.update(plaintext, 'utf8', 'hex');
      encrypted += cipher.final('hex');

      // Get auth tag
      const tag = cipher.getAuthTag();

      // Combine IV + encrypted + tag
      const result = Buffer.concat([
        iv,
        Buffer.from(encrypted, 'hex'),
        tag
      ]).toString('base64');

      return result;
    } catch (error) {
      this.logger.error(`Encryption failed: ${error.message}`);
      throw new InternalServerErrorException('Encryption failed');
    }
  }

  async decrypt(ciphertext: string): Promise<string> {
    try {
      // Get encryption key from KMS
      const dataKey = await this.getDataKey();

      // Decode base64
      const buffer = Buffer.from(ciphertext, 'base64');

      // Extract IV, encrypted data, and tag
      const iv = buffer.slice(0, this.ivLength);
      const tag = buffer.slice(-this.tagLength);
      const encrypted = buffer.slice(this.ivLength, -this.tagLength);

      // Create decipher
      const decipher = crypto.createDecipheriv(
        this.algorithm,
        dataKey,
        iv
      );
      decipher.setAuthTag(tag);

      // Decrypt
      let decrypted = decipher.update(encrypted, undefined, 'utf8');
      decrypted += decipher.final('utf8');

      return decrypted;
    } catch (error) {
      this.logger.error(`Decryption failed: ${error.message}`);
      throw new InternalServerErrorException('Decryption failed');
    }
  }

  private async getDataKey(): Promise<Buffer> {
    // In production, fetch from KMS
    // For now, use environment variable
    const key = process.env.ENCRYPTION_KEY;
    if (!key) {
      throw new Error('Encryption key not configured');
    }
    return Buffer.from(key, 'hex');
  }

  async hash(data: string): Promise<string> {
    return crypto
      .createHash('sha256')
      .update(data)
      .digest('hex');
  }

  async compareHash(data: string, hash: string): Promise<boolean> {
    const dataHash = await this.hash(data);
    return crypto.timingSafeEqual(
      Buffer.from(dataHash),
      Buffer.from(hash)
    );
  }
}
```

### 6.3 Audit Logging Service

```typescript
@Injectable()
export class AuditLogger {
  constructor(
    private readonly auditRepository: AuditLogRepository,
    private readonly eventPublisher: EventPublisher,
    private readonly logger: Logger
  ) {}

  async log(event: AuditEvent): Promise<void> {
    try {
      const auditLog: AuditLog = {
        eventId: generateId(),
        timestamp: new Date(),
        eventType: event.eventType,
        severity: event.severity || 'INFO',
        userId: event.userId,
        resourceType: event.resourceType,
        resourceId: event.resourceId,
        action: event.action,
        ipAddress: event.ipAddress,
        userAgent: event.userAgent,
        requestId: event.requestId,
        correlationId: event.correlationId,
        dataClassification: event.dataClassification,
        details: event.details || {},
      };

      // Save to database
      await this.auditRepository.save(auditLog);

      // Publish to event stream for real-time monitoring
      if (event.severity === 'CRITICAL' || event.severity === 'ERROR') {
        await this.eventPublisher.publish(
          new AuditEventPublished(auditLog)
        );
      }

      this.logger.log(
        `Audit: ${event.eventType} - User: ${event.userId} - Resource: ${event.resourceId}`
      );
    } catch (error) {
      // Audit logging failure should not break the main flow
      this.logger.error(`Audit logging failed: ${error.message}`);
    }
  }

  async query(filters: AuditQueryFilters): Promise<PaginatedResult<AuditLog>> {
    return this.auditRepository.search(filters);
  }
}

interface AuditEvent {
  eventType: string;
  severity?: 'INFO' | 'WARN' | 'ERROR' | 'CRITICAL';
  userId?: string;
  resourceType?: string;
  resourceId?: string;
  action?: string;
  ipAddress?: string;
  userAgent?: string;
  requestId?: string;
  correlationId?: string;
  dataClassification?: string;
  details?: Record<string, any>;
}
```

---

## 7. INTEGRATION ARCHITECTURE

### 7.1 Event Publisher

```typescript
@Injectable()
export class EventPublisher {
  constructor(
    private readonly kafka: Kafka,
    private readonly logger: Logger
  ) {}

  async publish(event: DomainEvent): Promise<void> {
    try {
      const producer = this.kafka.producer();
      await producer.connect();

      const message = {
        key: event.aggregateId,
        value: JSON.stringify({
          eventId: event.eventId,
          eventType: event.eventType,
          eventVersion: event.eventVersion,
          timestamp: event.timestamp,
          aggregateId: event.aggregateId,
          aggregateType: event.aggregateType,
          data: event.data,
          metadata: event.metadata,
        }),
        headers: {
          'event-type': event.eventType,
          'correlation-id': event.metadata.correlationId,
        },
      };

      await producer.send({
        topic: this.getTopicForEvent(event.eventType),
        messages: [message],
      });

      await producer.disconnect();

      this.logger.log(`Event published: ${event.eventType} - ${event.eventId}`);
    } catch (error) {
      this.logger.error(`Event publishing failed: ${error.message}`);
      throw error;
    }
  }

  private getTopicForEvent(eventType: string): string {
    const topicMap: Record<string, string> = {
      'PatientCreated': 'patients.created',
      'PatientUpdated': 'patients.updated',
      'PatientDeleted': 'patients.deleted',
      'AppointmentScheduled': 'appointments.scheduled',
      'AppointmentCancelled': 'appointments.cancelled',
      'MedicalRecordCreated': 'medical-records.created',
    };
    return topicMap[eventType] || 'default';
  }
}

interface DomainEvent {
  eventId: string;
  eventType: string;
  eventVersion: string;
  timestamp: Date;
  aggregateId: string;
  aggregateType: string;
  data: any;
  metadata: {
    userId: string;
    correlationId: string;
    [key: string]: any;
  };
}
```

### 7.2 External Service Adapters

```typescript
// ============= NOTIFICATION ADAPTER =============

@Injectable()
export class NotificationAdapter {
  constructor(
    private readonly httpService: HttpService,
    private readonly circuitBreaker: CircuitBreaker,
    private readonly logger: Logger
  ) {}

  async sendAppointmentConfirmation(
    email: string,
    appointment: Appointment
  ): Promise<void> {
    return this.circuitBreaker.execute(
      'notification-service',
      async () => {
        try {
          await this.httpService.post(
            `${process.env.NOTIFICATION_SERVICE_URL}/emails`,
            {
              to: email,
              template: 'appointment-confirmation',
              data: {
                appointmentId: appointment.id,
                scheduledDateTime: appointment.scheduledDateTime,
                provider: appointment.providerId,
              },
            },
            {
              timeout: 5000,
              headers: {
                'Authorization': `Bearer ${process.env.NOTIFICATION_SERVICE_TOKEN}`,
              },
            }
          );

          this.logger.log(`Notification sent: ${email}`);
        } catch (error) {
          this.logger.error(`Notification failed: ${error.message}`);
          throw error;
        }
      }
    );
  }

  async sendAppointmentCancellation(
    email: string,
    appointment: Appointment
  ): Promise<void> {
    // Similar implementation
  }
}

// ============= FILE STORAGE ADAPTER =============

@Injectable()
export class FileStorageAdapter {
  constructor(
    private readonly s3Client: S3Client,
    private readonly logger: Logger
  ) {}

  async upload(
    file: Buffer,
    options: UploadOptions
  ): Promise<string> {
    try {
      const key = `${options.folder}/${generateId()}-${options.fileName}`;

      await this.s3Client.send(
        new PutObjectCommand({
          Bucket: process.env.S3_BUCKET,
          Key: key,
          Body: file,
          ServerSideEncryption: 'AES256',
          Metadata: options.metadata,
        })
      );

      this.logger.log(`File uploaded: ${key}`);
      return key;
    } catch (error) {
      this.logger.error(`File upload failed: ${error.message}`);
      throw new InternalServerErrorException('File upload failed');
    }
  }

  async generateSignedUrl(
    key: string,
    options: { expiresIn: number }
  ): Promise<string> {
    try {
      const command = new GetObjectCommand({
        Bucket: process.env.S3_BUCKET,
        Key: key,
      });

      const url = await getSignedUrl(this.s3Client, command, {
        expiresIn: options.expiresIn,
      });

      return url;
    } catch (error) {
      this.logger.error(`Signed URL generation failed: ${error.message}`);
      throw new InternalServerErrorException('URL generation failed');
    }
  }
}

interface UploadOptions {
  fileName: string;
  folder: string;
  encryption?: string;
  metadata?: Record<string, string>;
}
```

### 7.3 Circuit Breaker Implementation

```typescript
@Injectable()
export class CircuitBreaker {
  private circuits: Map<string, CircuitState> = new Map();

  constructor(private readonly logger: Logger) {}

  async execute<T>(
    serviceName: string,
    operation: () => Promise<T>,
    config: CircuitBreakerConfig = DEFAULT_CONFIG
  ): Promise<T> {
    const circuit = this.getOrCreateCircuit(serviceName, config);

    // Check circuit state
    if (circuit.state === 'OPEN') {
      if (Date.now() - circuit.lastFailureTime < config.resetTimeout) {
        throw new ServiceUnavailableException(
          `Circuit breaker is OPEN for ${serviceName}`
        );
      }
      // Transition to HALF_OPEN
      circuit.state = 'HALF_OPEN';
      circuit.halfOpenAttempts = 0;
    }

    try {
      const result = await operation();

      // Success - update circuit
      circuit.successCount++;
      circuit.failureCount = 0;

      if (circuit.state === 'HALF_OPEN') {
        circuit.halfOpenAttempts++;
        if (circuit.halfOpenAttempts >= config.halfOpenRequests) {
          circuit.state = 'CLOSED';
          this.logger.log(`Circuit breaker CLOSED for ${serviceName}`);
        }
      }

      return result;
    } catch (error) {
      // Failure - update circuit
      circuit.failureCount++;
      circuit.lastFailureTime = Date.now();

      const failureRate = circuit.failureCount / 
        (circuit.successCount + circuit.failureCount);

      if (failureRate >= config.failureThreshold) {
        circuit.state = 'OPEN';
        this.logger.error(
          `Circuit breaker OPENED for ${serviceName} - Failure rate: ${failureRate}`
        );
      }

      throw error;
    }
  }

  private getOrCreateCircuit(
    serviceName: string,
    config: CircuitBreakerConfig
  ): CircuitState {
    if (!this.circuits.has(serviceName)) {
      this.circuits.set(serviceName, {
        state: 'CLOSED',
        successCount: 0,
        failureCount: 0,
        lastFailureTime: 0,
        halfOpenAttempts: 0,
        config,
      });
    }
    return this.circuits.get(serviceName)!;
  }
}

interface CircuitState {
  state: 'CLOSED' | 'OPEN' | 'HALF_OPEN';
  successCount: number;
  failureCount: number;
  lastFailureTime: number;
  halfOpenAttempts: number;
  config: CircuitBreakerConfig;
}

interface CircuitBreakerConfig {
  failureThreshold: number; // 0.5 = 50%
  resetTimeout: number; // milliseconds
  halfOpenRequests: number;
}

const DEFAULT_CONFIG: CircuitBreakerConfig = {
  failureThreshold: 0.5,
  resetTimeout: 30000, // 30 seconds
  halfOpenRequests: 3,
};
```

---

## 8. DEPLOYMENT ARCHITECTURE

### 8.1 Kubernetes Manifests

```yaml
# ============= DEPLOYMENT =============

apiVersion: apps/v1
kind: Deployment
metadata:
  name: patient-service
  namespace: healthcare
  labels:
    app: patient-service
    version: v1
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: patient-service
  template:
    metadata:
      labels:
        app: patient-service
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/metrics"
    spec:
      serviceAccountName: patient-service
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: patient-service
        image: healthcare/patient-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        - name: NODE_ENV
          value: "production"
        - name: PORT
          value: "8080"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-credentials
              key: url
        - name: REDIS_URL
          valueFrom:
            secretKeyRef:
              name: redis-credentials
              key: url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        - name: ENCRYPTION_KEY
          valueFrom:
            secretKeyRef:
              name: encryption-key
              key: key
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /health/startup
            port: 8080
          initialDelaySeconds: 0
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
        volumeMounts:
        - name: config
          mountPath: /app/config
          readOnly: true
      volumes:
      - name: config
        configMap:
          name: patient-service-config

---
# ============= SERVICE =============

apiVersion: v1
kind: Service
metadata:
  name: patient-service
  namespace: healthcare
  labels:
    app: patient-service
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: patient-service

---
# ============= HORIZONTAL POD AUTOSCALER =============

apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: patient-service-hpa
  namespace: healthcare
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: patient-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max

---
# ============= POD DISRUPTION BUDGET =============

apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: patient-service-pdb
  namespace: healthcare
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: patient-service

---
# ============= NETWORK POLICY =============

apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: patient-service-netpol
  namespace: healthcare
spec:
  podSelector:
    matchLabels:
      app: patient-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - namespaceSelector:
        matchLabels:
          name: redis
    ports:
    - protocol: TCP
      port: 6379
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

### 8.2 Infrastructure as Code (Terraform)

```hcl
# ============= VPC =============

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "healthcare-vpc"
    Environment = var.environment
  }
}

# ============= SUBNETS =============

resource "aws_subnet" "private" {
  count             = 3
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 1}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name        = "healthcare-private-${count.index + 1}"
    Environment = var.environment
    Tier        = "private"
  }
}

resource "aws_subnet" "public" {
  count                   = 3
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.${count.index + 101}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name        = "healthcare-public-${count.index + 1}"
    Environment = var.environment
    Tier        = "public"
  }
}

# ============= RDS (PostgreSQL) =============

resource "aws_db_instance" "main" {
  identifier     = "healthcare-db"
  engine         = "postgres"
  engine_version = "15.3"
  instance_class = "db.r6g.xlarge"

  allocated_storage     = 100
  max_allocated_storage = 1000
  storage_type          = "gp3"
  storage_encrypted     = true
  kms_key_id            = aws_kms_key.rds.arn

  db_name  = "healthcare"
  username = "admin"
  password = random_password.db_password.result

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period = 30
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  performance_insights_enabled    = true
  monitoring_interval             = 60
  monitoring_role_arn             = aws_iam_role.rds_monitoring.arn

  deletion_protection = true
  skip_final_snapshot = false
  final_snapshot_identifier = "healthcare-db-final-snapshot"

  tags = {
    Name        = "healthcare-db"
    Environment = var.environment
  }
}

# ============= ELASTICACHE (Redis) =============

resource "aws_elasticache_replication_group" "main" {
  replication_group_id       = "healthcare-redis"
  replication_group_description = "Healthcare Redis cluster"

  engine               = "redis"
  engine_version       = "7.0"
  node_type            = "cache.r6g.large"
  number_cache_clusters = 3
  port                 = 6379

  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.redis.id]

  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  auth_token_enabled         = true
  auth_token                 = random_password.redis_password.result

  automatic_failover_enabled = true
  multi_az_enabled           = true

  snapshot_retention_limit = 7
  snapshot_window          = "03:00-05:00"
  maintenance_window       = "sun:05:00-sun:07:00"

  tags = {
    Name        = "healthcare-redis"
    Environment = var.environment
  }
}

# ============= EKS CLUSTER =============

resource "aws_eks_cluster" "main" {
  name     = "healthcare-eks"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.28"

  vpc_config {
    subnet_ids              = aws_subnet.private[*].id
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = ["0.0.0.0/0"]
    security_group_ids      = [aws_security_group.eks_cluster.id]
  }

  encryption_config {
    provider {
      key_arn = aws_kms_key.eks.arn
    }
    resources = ["secrets"]
  }

  enabled_cluster_log_types = [
    "api",
    "audit",
    "authenticator",
    "controllerManager",
    "scheduler"
  ]

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
    aws_iam_role_policy_attachment.eks_vpc_resource_controller,
  ]

  tags = {
    Name        = "healthcare-eks"
    Environment = var.environment
  }
}

# ============= EKS NODE GROUP =============

resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "healthcare-nodes"
  node_role_arn   = aws_iam_role.eks_node_group.arn
  subnet_ids      = aws_subnet.private[*].id

  instance_types = ["t3.xlarge"]
  capacity_type  = "ON_DEMAND"

  scaling_config {
    desired_size = 3
    max_size     = 10
    min_size     = 3
  }

  update_config {
    max_unavailable = 1
  }

  launch_template {
    id      = aws_launch_template.eks_nodes.id
    version = "$Latest"
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry_policy,
  ]

  tags = {
    Name        = "healthcare-nodes"
    Environment = var.environment
  }
}
```

---

## 9. OPERATIONAL PROCEDURES

### 9.1 Monitoring & Alerting

```yaml
# ============= PROMETHEUS RULES =============

groups:
  - name: healthcare_api_alerts
    interval: 30s
    rules:
      # High Error Rate
      - alert: HighErrorRate
        expr: |
          (
            sum(rate(http_requests_total{status=~"5.."}[5m]))
            /
            sum(rate(http_requests_total[5m]))
          ) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }} (threshold: 5%)"

      # High Response Time
      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95,
            sum(rate(http_request_duration_seconds_bucket[5m])) by (le)
          ) > 1.0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "P95 response time is {{ $value }}s (threshold: 1s)"

      # Database Connection Pool Exhaustion
      - alert: DatabaseConnectionPoolExhaustion
        expr: |
          (
            sum(database_connections_active)
            /
            sum(database_connections_max)
          ) > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Connection pool usage is {{ $value | humanizePercentage }}"

      # High Memory Usage
      - alert: HighMemoryUsage
        expr: |
          (
            container_memory_usage_bytes{pod=~"patient-service-.*"}
            /
            container_spec_memory_limit_bytes{pod=~"patient-service-.*"}
          ) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Memory usage is {{ $value | humanizePercentage }}"

      # Pod Restart Rate
      - alert: HighPodRestartRate
        expr: |
          rate(kube_pod_container_status_restarts_total{pod=~"patient-service-.*"}[15m]) > 0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Pod restart rate is high"
          description: "Pod {{ $labels.pod }} is restarting frequently"

      # Circuit Breaker Open
      - alert: CircuitBreakerOpen
        expr: circuit_breaker_state{state="open"} == 1
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Circuit breaker is open"
          description: "Circuit breaker for {{ $labels.service }} is open"
```

### 9.2 Backup & Recovery

```bash
#!/bin/bash
# ============= DATABASE BACKUP SCRIPT =============

set -e

# Configuration
DB_HOST="healthcare-db.cluster-xxx.us-east-1.rds.amazonaws.com"
DB_NAME="healthcare"
DB_USER="admin"
BACKUP_DIR="/backups/postgresql"
RETENTION_DAYS=30
S3_BUCKET="healthcare-backups"

# Generate backup filename
BACKUP_FILE="${DB_NAME}_$(date +%Y%m%d_%H%M%S).sql.gz"

echo "Starting database backup: ${BACKUP_FILE}"

# Create backup directory if not exists
mkdir -p ${BACKUP_DIR}

# Perform backup
pg_dump -h ${DB_HOST} \
        -U ${DB_USER} \
        -d ${DB_NAME} \
        --format=custom \
        --compress=9 \
        --verbose \
        | gzip > ${BACKUP_DIR}/${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "Backup completed successfully"
    
    # Upload to S3
    aws s3 cp ${BACKUP_DIR}/${BACKUP_FILE} \
               s3://${S3_BUCKET}/postgresql/${BACKUP_FILE} \
               --storage-class STANDARD_IA
    
    if [ $? -eq 0 ]; then
        echo "Backup uploaded to S3"
        
        # Remove local backup
        rm ${BACKUP_DIR}/${BACKUP_FILE}
        
        # Clean up old backups from S3
        aws s3 ls s3://${S3_BUCKET}/postgresql/ | \
            while read -r line; do
                createDate=$(echo $line | awk '{print $1" "$2}')
                createDate=$(date -d "$createDate" +%s)
                olderThan=$(date -d "-${RETENTION_DAYS} days" +%s)
                if [[ $createDate -lt $olderThan ]]; then
                    fileName=$(echo $line | awk '{print $4}')
                    if [[ $fileName != "" ]]; then
                        aws s3 rm s3://${S3_BUCKET}/postgresql/$fileName
                        echo "Deleted old backup: $fileName"
                    fi
                fi
            done
    else
        echo "Failed to upload backup to S3"
        exit 1
    fi
else
    echo "Backup failed"
    exit 1
fi

echo "Backup process completed"
```

### 9.3 Disaster Recovery Procedure

```markdown
# DISASTER RECOVERY PROCEDURE

## Objective
Restore Healthcare Patient Management System to operational state within RTO (1 hour) and RPO (15 minutes).

## Prerequisites
- Access to AWS Console
- Access to Kubernetes cluster
- Database backup available
- Terraform state available

## Recovery Steps

### Step 1: Assess Damage (5 minutes)
1. Identify affected components
2. Determine root cause
3. Decide on recovery strategy

### Step 2: Activate DR Site (10 minutes)
1. Switch DNS to DR region
   ```bash
   aws route53 change-resource-record-sets \
     --hosted-zone-id Z1234567890ABC \
     --change-batch file://dns-failover.json
   ```

2. Verify DR infrastructure
   ```bash
   terraform plan -target=module.dr_region
   ```

### Step 3: Restore Database (20 minutes)
1. Identify latest backup
   ```bash
   aws s3 ls s3://healthcare-backups/postgresql/ --recursive | sort | tail -n 1
   ```

2. Download backup
   ```bash
   aws s3 cp s3://healthcare-backups/postgresql/healthcare_20240120_143000.sql.gz /tmp/
   ```

3. Restore database
   ```bash
   gunzip -c /tmp/healthcare_20240120_143000.sql.gz | \
   pg_restore -h dr-db-host -U admin -d healthcare --clean --if-exists
   ```

4. Verify data integrity
   ```sql
   SELECT COUNT(*) FROM patients;
   SELECT MAX(created_at) FROM audit_logs;
   ```

### Step 4: Deploy Application (15 minutes)
1. Deploy to Kubernetes
   ```bash
   kubectl apply -f k8s/production/
   ```

2. Verify pods are running
   ```bash
   kubectl get pods -n healthcare
   ```

3. Check health endpoints
   ```bash
   curl https://api-dr.healthcare.example.com/health
   ```

### Step 5: Verify Functionality (10 minutes)
1. Run smoke tests
   ```bash
   npm run test:smoke -- --env=dr
   ```

2. Verify critical flows:
   - Patient creation
   - Appointment scheduling
   - Medical record access

3. Check monitoring dashboards

### Step 6: Communication
1. Notify stakeholders of recovery status
2. Update status page
3. Document incident

## Rollback Procedure
If recovery fails:
1. Revert DNS changes
2. Investigate issues
3. Retry recovery or escalate

## Post-Recovery
1. Conduct post-mortem
2. Update runbooks
3. Test DR procedure
4. Implement preventive measures
```

---

## 10. COMPLIANCE & GOVERNANCE

### 10.1 HIPAA Compliance Checklist

```markdown
# HIPAA COMPLIANCE CHECKLIST

## Technical Safeguards

### Access Control (§164.312(a))
- [x] Unique user identification
- [x] Emergency access procedure (break-glass)
- [x] Automatic logoff after inactivity
- [x] Encryption and decryption mechanisms

### Audit Controls (§164.312(b))
- [x] Hardware, software, and procedural mechanisms
- [x] Record and examine activity in systems with ePHI
- [x] Audit log retention for 7 years

### Integrity (§164.312(c))
- [x] Mechanisms to authenticate ePHI
- [x] Protection from improper alteration or destruction
- [x] Data lineage tracking

### Person or Entity Authentication (§164.312(d))
- [x] Verify person/entity seeking access
- [x] Multi-factor authentication for privileged access

### Transmission Security (§164.312(e))
- [x] Integrity controls (checksums, digital signatures)
- [x] Encryption (TLS 1.3 for data in transit)
- [x] AES-256 encryption for data at rest

## Administrative Safeguards

### Security Management Process (§164.308(a)(1))
- [x] Risk analysis conducted
- [x] Risk management plan implemented
- [x] Sanction policy for violations
- [x] Information system activity review

### Workforce Security (§164.308(a)(3))
- [x] Authorization and supervision procedures
- [x] Workforce clearance procedure
- [x] Termination procedures

### Information Access Management (§164.308(a)(4))
- [x] Access authorization procedures
- [x] Access establishment and modification
- [x] Role-based access control (RBAC)
- [x] Attribute-based access control (ABAC)

### Security Awareness and Training (§164.308(a)(5))
- [x] Security reminders
- [x] Protection from malicious software
- [x] Log-in monitoring
- [x] Password management

### Security Incident Procedures (§164.308(a)(6))
- [x] Response and reporting procedures
- [x] Incident response plan documented

### Contingency Plan (§164.308(a)(7))
- [x] Data backup plan
- [x] Disaster recovery plan
- [x] Emergency mode operation plan
- [x] Testing and revision procedures
- [x] Applications and data criticality analysis

### Business Associate Contracts (§164.308(b))
- [x] Written contracts with business associates
- [x] BAA includes required provisions

## Physical Safeguards

### Facility Access Controls (§164.310(a))
- [x] Contingency operations
- [x] Facility security plan
- [x] Access control and validation procedures
- [x] Maintenance records

### Workstation Use (§164.310(b))
- [x] Policies and procedures for workstation use

### Workstation Security (§164.310(c))
- [x] Physical safeguards for workstations

### Device and Media Controls (§164.310(d))
- [x] Disposal procedures
- [x] Media re-use procedures
- [x] Accountability
- [x] Data backup and storage

## Documentation

- [x] Policies and procedures documented
- [x] Documentation retained for 6 years
- [x] Documentation reviewed and updated annually
```

### 10.2 Data Retention Policy

```yaml
Data Retention Policy:
  Patient Records:
    Retention Period: 7 years from last treatment
    Storage Tier: Standard → Infrequent Access → Glacier
    Deletion Method: Cryptographic erasure
    Legal Hold: Check before deletion
    
  Audit Logs:
    Retention Period: 7 years
    Storage Tier: Standard (hot) → Archive (cold)
    Deletion Method: Secure deletion
    Immutability: Write-once-read-many (WORM)
    
  Consent Records:
    Retention Period: 7 years after withdrawal
    Storage Tier: Standard
    Deletion Method: Secure deletion with audit trail
    Legal Requirement: Yes
    
  Appointment Records:
    Retention Period: 3 years
    Storage Tier: Standard → Infrequent Access
    Deletion Method: Soft delete → Hard delete
    
  System Logs:
    Retention Period: 90 days
    Storage Tier: Hot storage
    Deletion Method: Automatic expiration
    
  Backups:
    Retention Period: 30 days
    Storage Tier: Backup storage
    Deletion Method: Automatic expiration
    Encryption: Yes (AES-256)

Deletion Process:
  1. Automated Identification:
     - Daily job identifies expired records
     - Generates deletion report
     
  2. Legal Hold Check:
     - Query legal hold database
     - Skip records under legal hold
     
  3. Secure Deletion:
     - Cryptographic erasure for encrypted data
     - Multi-pass overwrite for unencrypted data
     - Verify deletion completion
     
  4. Audit Trail:
     - Log deletion event
     - Record deletion timestamp
     - Store deletion certificate
     
  5. Compliance Report:
     - Generate monthly deletion report
     - Review by compliance officer
     - Archive report for 7 years
```

---

## 11. APPENDICES

### Appendix A: Glossary

| Term | Definition |
|------|------------|
| ABAC | Attribute-Based Access Control - Access control based on attributes of users, resources, and environment |
| API | Application Programming Interface |
| CRUD | Create, Read, Update, Delete |
| DTO | Data Transfer Object |
| EHR | Electronic Health Record |
| HIPAA | Health Insurance Portability and Accountability Act |
| JWT | JSON Web Token |
| MRN | Medical Record Number |
| OAuth | Open Authorization - Industry-standard protocol for authorization |
| PHI | Protected Health Information |
| PII | Personally Identifiable Information |
| RBAC | Role-Based Access Control |
| REST | Representational State Transfer |
| RTO | Recovery Time Objective |
| RPO | Recovery Point Objective |
| SLA | Service Level Agreement |
| TLS | Transport Layer Security |
| TOGAF | The Open Group Architecture Framework |

### Appendix B: References

1. **HIPAA Security Rule**
   - https://www.hhs.gov/hipaa/for-professionals/security/index.html

2. **NIST Cybersecurity Framework**
   - https://www.nist.gov/cyberframework

3. **OWASP Top 10**
   - https://owasp.org/www-project-top-ten/

4. **OpenAPI Specification 3.0**
   - https://swagger.io/specification/

5. **TOGAF 9.2**
   - https://www.opengroup.org/togaf

6. **ISO 27001**
   - https://www.iso.org/isoiec-27001-information-security.html

### Appendix C: Change Log

| Version | Date | Author | Changes |
|---------|------|--------|----------|
| 1.0.0 | 2024-01-20 | Senior Backend Solution Architect | Initial LLD release |

### Appendix D: Approval Signatures

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Solution Architect | | | |
| Security Architect | | | |
| Compliance Officer | | | |
| Engineering Manager | | | |
| CTO | | | |

---

## DOCUMENT END

**Document Classification:** Confidential - Internal Use Only

**Distribution:** Architecture Team, Engineering Team, Security Team, Compliance Team

**Review Cycle:** Quarterly

**Next Review Date:** 2024-04-20

---

*This Low-Level Design document is a living document and should be updated as the system evolves. All changes must be reviewed and approved by the Architecture Review Board.*