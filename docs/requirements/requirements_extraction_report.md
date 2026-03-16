# REQUIREMENTS EXTRACTION REPORT

## JIRA ISSUE INFORMATION
**JIRA Issue Key:** SCRUM-96  
**User Story Summary:** Implement Core Shopping Cart Backend Services Using Spring Boot MVC  
**Issue Type:** Task  
**Status:** To Do  
**Project:** AAVA QE (SCRUM)

---

## EXTRACTED REQUIREMENTS

### FUNCTIONAL REQUIREMENTS

#### 1. User Management Requirements

**FR-001: User Registration**
- The system shall allow new user registration with username, password, full name, and email
- The system shall create a user record in the users table upon successful registration
- The system shall enforce username uniqueness across all users

**FR-002: User Authentication**
- The system shall authenticate users using username and password credentials
- The system shall return user details upon successful authentication
- The system shall not store session data at the database level (stateless authentication)

**FR-003: User Profile View**
- The system shall allow authenticated users to view their profile information including username, full name, email, and created date

**FR-004: User Profile Update**
- The system shall allow users to update their full name and email
- The system shall enforce username immutability (username cannot be changed)
- The system shall exclude password changes from profile update functionality

#### 2. Product Catalog Requirements

**FR-005: Product Search**
- The system shall provide keyword-based product search functionality
- The system shall perform case-insensitive search operations
- The system shall return product name, description, price, and available quantity in search results
- The system shall only return products that exist in the database

#### 3. Shopping Cart Management Requirements

**FR-006: Lazy Cart Creation**
- The system shall not create a cart by default upon user login
- The system shall create a cart only when a user adds their first product

**FR-007: Add Product to Cart**
- The system shall allow users to add products to their cart with specified quantity
- The system shall enforce quantity to be greater than zero
- The system shall automatically create a cart if one does not exist when adding the first product
- The system shall assume stock sufficiency without enforcement (inventory locking out of scope)

**FR-008: Update Cart Item Quantity**
- The system shall allow users to increase or decrease the quantity of cart items
- The system shall enforce that updated quantity remains greater than zero

**FR-009: Remove Product from Cart**
- The system shall allow users to remove individual products from their cart
- The system shall retain other products in the cart when one product is removed

**FR-010: View Cart**
- The system shall display all products in the user's cart
- The system shall calculate and display per-item totals (price × quantity)
- The system shall calculate and display the grand total of all cart items

**FR-011: Auto-Delete Empty Cart**
- The system shall automatically delete all cart items when the last item is removed
- The system shall automatically delete the cart record when it becomes empty

**FR-012: Cart Cleanup on Logout**
- The system shall delete all cart items when a user logs out
- The system shall delete the cart record when a user logs out
- The system shall ensure carts do not persist across user sessions

#### 4. API and Architecture Requirements

**FR-013: RESTful API Exposure**
- The system shall expose RESTful APIs for all user, product, and cart operations
- The system shall conform to MVC layering architecture (Controller → Service → Repository)

---

### NON-FUNCTIONAL REQUIREMENTS

#### 1. Data Integrity and Consistency

**NFR-001: Username Uniqueness Constraint**
- The system shall enforce username uniqueness at the database level using unique constraints

**NFR-002: User Existence Validation**
- The system shall validate user existence before performing any cart operation
- The system shall enforce foreign key constraints between carts and users

**NFR-003: Cart Ownership Rules**
- The system shall enforce one active cart per user maximum
- The system shall ensure each cart belongs to exactly one user
- The system shall enforce that carts cannot exist without at least one cart item

**NFR-004: Cart Item Integrity**
- The system shall ensure each cart item belongs to exactly one cart
- The system shall validate product existence before adding to cart
- The system shall enforce quantity greater than zero at the database level

**NFR-005: Product Data Protection**
- The system shall ensure product prices cannot be modified through user actions
- The system shall validate product existence before any cart operation

#### 2. Performance and Scalability

**NFR-006: Database-First Validation**
- The system shall enforce all business rules at both service and database levels
- The system shall use database constraints for data integrity enforcement
- The system shall verify all business rules through database state

**NFR-007: Stateless Architecture**
- The system shall implement stateless authentication at the database level
- The system shall not maintain session state in the database

#### 3. Technology and Platform

**NFR-008: Technology Stack**
- The system shall be implemented using Java Spring Boot framework
- The system shall follow MVC (Model-View-Controller) architecture pattern
- The system shall use a relational database for data persistence

**NFR-009: API Design Standards**
- The system shall implement RESTful API design principles
- The system shall ensure APIs reflect database outcomes accurately

#### 4. Security and Compliance

**NFR-010: Data Isolation**
- The system shall ensure seed users do not automatically receive carts
- The system shall maintain proper user data isolation

**NFR-011: Session Management**
- The system shall implement stateless login mechanism
- The system shall ensure cart data does not persist across logout/login sessions

---

## DATA INTEGRITY & BUSINESS RULES SUMMARY

### User Rules
- Username must be unique (database constraint)
- User must exist before any cart operation (foreign key enforcement)

### Cart Rules
- One active cart per user (business rule + database constraint)
- Cart belongs to exactly one user (foreign key constraint)
- Cart cannot exist without at least one cart item (enforced via auto-delete)

### Cart Item Rules
- Each cart item belongs to one cart (foreign key constraint)
- Product must exist before adding (foreign key constraint)
- Quantity must be greater than zero (check constraint)

### Product Rules
- Product must exist (primary key validation)
- Price cannot be modified through user actions (access control)

### System-Level Rules
- Seed users must not automatically receive carts
- Login is stateless at database level
- All business rules must be verifiable through database state
- APIs must reflect database outcomes

---

## EXPLICITLY OUT OF SCOPE

The following features are explicitly excluded from this implementation:
1. Checkout / Orders functionality
2. Payment processing
3. Inventory reservation and locking
4. Admin product management
5. Password reset/change functionality
6. User roles and permissions
7. Cart persistence across sessions

---

## ACCEPTANCE CRITERIA

1. All APIs conform to MVC layering (Controller → Service → Repository)
2. Database constraints enforce uniqueness and foreign-key rules
3. Cart lifecycle rules behave as defined
4. Empty carts are removed automatically
5. Logout deletes any active cart
6. Product search is case-insensitive
7. Stateless authentication is preserved
8. No out-of-scope functionality is implemented

---

## EXTRACTION METADATA

**Extraction Timestamp:** 2024-12-19T10:45:00Z  
**Extractor ID:** AGENT-REQ-001  
**Source System:** JIRA (Ascendion Confluence)  
**Issue Key:** SCRUM-96  
**Issue Type:** Task  
**Current Status:** To Do  
**Project:** AAVA QE (SCRUM)  

**Compliance Flags:**
- **PII/PHI/PCI Screening:** PASSED - No sensitive personal information, health records, or payment card data detected in requirements
- **Data Classification:** INTERNAL - Business application requirements
- **Regulatory Considerations:** Standard software development practices apply
- **Audit Trail:** Complete issue history available in JIRA
- **Traceability:** All requirements mapped to source user story sections

**Quality Metrics:**
- Total Functional Requirements: 13
- Total Non-Functional Requirements: 11
- Requirements Clarity Score: HIGH (all requirements are explicit and testable)
- Ambiguity Detection: NONE (all requirements have clear acceptance criteria)
- Completeness Score: HIGH (comprehensive coverage of in-scope features)

**Risk Assessment:**
- **Technical Risk:** LOW - Standard Spring Boot MVC implementation
- **Scope Creep Risk:** LOW - Clear out-of-scope boundaries defined
- **Integration Risk:** LOW - Self-contained backend service
- **Compliance Risk:** LOW - No regulated data handling required

---

## REQUIREMENTS TRACEABILITY MATRIX

| Requirement ID | Category | Source Section | Priority | Testability |
|---------------|----------|----------------|----------|-------------|
| FR-001 | Functional | User Management - Sign-Up | HIGH | Testable |
| FR-002 | Functional | User Management - Sign-In | HIGH | Testable |
| FR-003 | Functional | User Management - View Profile | MEDIUM | Testable |
| FR-004 | Functional | User Management - Update Profile | MEDIUM | Testable |
| FR-005 | Functional | Product Catalog - Search | HIGH | Testable |
| FR-006 | Functional | Cart Management - Lazy Creation | HIGH | Testable |
| FR-007 | Functional | Cart Management - Add Product | HIGH | Testable |
| FR-008 | Functional | Cart Management - Update Quantity | HIGH | Testable |
| FR-009 | Functional | Cart Management - Remove Product | HIGH | Testable |
| FR-010 | Functional | Cart Management - View Cart | HIGH | Testable |
| FR-011 | Functional | Cart Management - Auto-Delete | HIGH | Testable |
| FR-012 | Functional | Cart Management - Logout Cleanup | HIGH | Testable |
| FR-013 | Functional | System Architecture | HIGH | Testable |
| NFR-001 | Non-Functional | Data Integrity - User Rules | HIGH | Testable |
| NFR-002 | Non-Functional | Data Integrity - User Rules | HIGH | Testable |
| NFR-003 | Non-Functional | Data Integrity - Cart Rules | HIGH | Testable |
| NFR-004 | Non-Functional | Data Integrity - Cart Item Rules | HIGH | Testable |
| NFR-005 | Non-Functional | Data Integrity - Product Rules | MEDIUM | Testable |
| NFR-006 | Non-Functional | System-Level Rules | HIGH | Testable |
| NFR-007 | Non-Functional | System-Level Rules | HIGH | Testable |
| NFR-008 | Non-Functional | Technology Stack | HIGH | Verifiable |
| NFR-009 | Non-Functional | API Design | HIGH | Testable |
| NFR-010 | Non-Functional | Security | MEDIUM | Testable |
| NFR-011 | Non-Functional | Session Management | HIGH | Testable |

---

**Document Status:** FINAL  
**Review Status:** Ready for Engineering Review  
**Next Steps:** Forward to development team for sprint planning and technical design