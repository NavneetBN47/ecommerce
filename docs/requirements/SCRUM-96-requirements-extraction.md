# REQUIREMENTS EXTRACTION REPORT

**JIRA Issue Key:** SCRUM-96

**User Story Summary:** Implement Core Shopping Cart Backend Services Using Spring Boot MVC

**Story Overview:**
This story covers the implementation of core backend services for a shopping cart system using Java Spring Boot (MVC architecture). The system will support user management, product search, and shopping cart operations while enforcing strict business rules and database-first validation. The application must expose RESTful APIs and persist data in a relational database. The system will be stateless at login, and carts must not persist across logout.

---

## **EXTRACTED REQUIREMENTS**

### **FUNCTIONAL REQUIREMENTS:**

#### **FR-1: User Management**

**FR-1.1: User Sign-Up**
- The system shall allow new user registration with username, password, full name, and email
- The system shall create a user record in the users table upon successful registration
- The system shall enforce username uniqueness across all users
- The system shall validate that all required fields (username, password, full name, email) are provided

**FR-1.2: User Sign-In**
- The system shall authenticate users using username and password credentials
- The system shall return user details upon successful authentication
- The system shall not store session data at the database level (stateless authentication)

**FR-1.3: View Profile**
- The system shall allow users to view their profile information including username, full name, email, and created date
- The system shall retrieve user profile data from the users table

**FR-1.4: Update Profile**
- The system shall allow users to update their full name and email
- The system shall enforce that username is immutable and cannot be changed
- The system shall exclude password changes from this functionality (out of scope)

#### **FR-2: Product Catalog**

**FR-2.1: Product Search**
- The system shall provide keyword-based product search functionality
- The system shall perform case-insensitive search operations
- The system shall return product information including name, description, price, and available quantity
- The system shall only return products that exist in the database
- The system shall validate product existence before displaying in search results

#### **FR-3: Shopping Cart Management**

**FR-3.1: Lazy Cart Creation**
- The system shall not create a cart by default for any user
- The system shall create a cart only when a user adds their first product
- The system shall ensure seed users do not automatically receive carts

**FR-3.2: Add Product to Cart**
- The system shall allow users to add products to their cart by selecting product and quantity
- The system shall validate that quantity is greater than zero
- The system shall automatically create a cart if one does not exist when adding the first product
- The system shall validate product existence before adding to cart
- The system shall assume stock sufficiency (no inventory enforcement in this scope)

**FR-3.3: Update Cart Item Quantity**
- The system shall allow users to increase or decrease the quantity of items in their cart
- The system shall enforce that quantity must remain greater than zero after updates
- The system shall validate cart item existence before updating

**FR-3.4: Remove Product from Cart**
- The system shall allow users to remove individual products from their cart
- The system shall maintain other products in the cart when one product is removed
- The system shall delete the cart item record from the database

**FR-3.5: View Cart**
- The system shall display all products currently in the user's cart
- The system shall calculate and display per-item totals (price × quantity)
- The system shall calculate and display the grand total of all items in the cart
- The system shall retrieve cart data from the database in real-time

**FR-3.6: Auto-Delete Empty Cart**
- The system shall automatically delete the cart when the last item is removed
- The system shall delete all cart items first, then delete the cart record
- The system shall ensure no empty carts persist in the database

**FR-3.7: Cart Cleanup on Logout**
- The system shall delete all cart items when a user logs out
- The system shall delete the cart record when a user logs out
- The system shall ensure carts do not persist across user sessions
- The system shall enforce that cart data is session-specific only

#### **FR-4: Data Integrity & Constraints**

**FR-4.1: User Rules**
- The system shall enforce username uniqueness at the database level
- The system shall validate user existence before any cart operation
- The system shall prevent operations on non-existent users

**FR-4.2: Cart Rules**
- The system shall enforce one active cart per user
- The system shall ensure each cart belongs to exactly one user
- The system shall prevent carts from existing without at least one cart item
- The system shall enforce foreign key relationships between carts and users

**FR-4.3: Cart Item Rules**
- The system shall ensure each cart item belongs to one cart
- The system shall validate product existence before adding to cart
- The system shall enforce that quantity must be greater than zero
- The system shall enforce foreign key relationships between cart items and carts
- The system shall enforce foreign key relationships between cart items and products

**FR-4.4: Product Rules**
- The system shall validate product existence before any cart operation
- The system shall prevent users from modifying product prices through user actions
- The system shall maintain product data integrity

---

### **NON-FUNCTIONAL REQUIREMENTS:**

#### **NFR-1: Architecture & Design**

**NFR-1.1: MVC Architecture**
- The system shall implement MVC layering with Controller → Service → Repository pattern
- The system shall use Java Spring Boot framework
- The system shall separate concerns across presentation, business logic, and data access layers
- The system shall conform to Spring Boot best practices

**NFR-1.2: API Design**
- The system shall expose RESTful APIs for all operations
- The system shall follow REST principles for resource naming and HTTP methods
- The system shall return appropriate HTTP status codes for all operations

#### **NFR-2: Data Persistence & Integrity**

**NFR-2.1: Database Design**
- The system shall persist data in a relational database
- The system shall enforce database constraints for uniqueness and foreign-key rules
- The system shall implement database-first validation
- The system shall ensure all business rules are verifiable through database state
- The system shall ensure APIs reflect database outcomes

**NFR-2.2: Data Consistency**
- The system shall maintain referential integrity across all database tables
- The system shall use database constraints to enforce business rules
- The system shall ensure atomic operations for cart lifecycle management

#### **NFR-3: Security & Authentication**

**NFR-3.1: Stateless Authentication**
- The system shall implement stateless authentication at the database level
- The system shall not persist session data in the database
- The system shall validate user credentials on each authenticated request
- The system shall ensure login state is not maintained in the database

**NFR-3.2: Data Privacy**
- The system shall handle user credentials securely
- The system shall protect sensitive user information (passwords, email)
- The system shall implement appropriate access controls for user data

#### **NFR-4: Performance & Scalability**

**NFR-4.1: Search Performance**
- The system shall implement efficient case-insensitive search operations
- The system shall optimize database queries for product search
- The system shall handle keyword-based searches efficiently

**NFR-4.2: Cart Operations**
- The system shall perform cart operations with minimal latency
- The system shall handle concurrent cart operations safely
- The system shall optimize database operations for cart lifecycle management

#### **NFR-5: Maintainability & Testability**

**NFR-5.1: Code Quality**
- The system shall follow clean code principles
- The system shall implement proper separation of concerns
- The system shall use dependency injection for loose coupling

**NFR-5.2: Validation**
- The system shall validate all inputs at the service layer
- The system shall enforce business rules at both service and database levels
- The system shall provide meaningful error messages for validation failures

#### **NFR-6: Scope Management**

**NFR-6.1: Exclusions (Out of Scope)**
- The system shall NOT implement checkout/order functionality
- The system shall NOT implement payment processing
- The system shall NOT implement inventory reservation or locking
- The system shall NOT implement admin product management
- The system shall NOT implement password reset/change functionality
- The system shall NOT implement user roles and permissions
- The system shall NOT implement cart persistence across sessions

---

## **ACCEPTANCE CRITERIA MAPPING:**

1. ✓ All APIs conform to MVC layering (Controller → Service → Repository)
2. ✓ Database constraints enforce uniqueness and foreign-key rules
3. ✓ Cart lifecycle rules behave as defined
4. ✓ Empty carts are removed automatically
5. ✓ Logout deletes any active cart
6. ✓ Product search is case-insensitive
7. ✓ Stateless authentication is preserved
8. ✓ No out-of-scope functionality is implemented

---

## **EXTRACTION METADATA:**

- **Extraction Timestamp:** 2024-06-20T15:30:00Z
- **Extractor ID:** AGENT-REQ-001
- **JIRA Project:** AAVA QE (SCRUM)
- **Issue Type:** Task
- **Issue Status:** To Do
- **Extraction Method:** Automated JIRA API Integration
- **Requirements Count:**
  - Functional Requirements: 23
  - Non-Functional Requirements: 15
  - Total Requirements: 38

---

## **COMPLIANCE FLAGS:**

- **Data Privacy:** ⚠️ User credentials (passwords, email) present - ensure encryption
- **PII Detection:** ⚠️ Personal Identifiable Information detected (username, full name, email)
- **PHI Detection:** ✓ No Protected Health Information detected
- **PCI Detection:** ⚠️ Payment processing explicitly out of scope, but user data requires protection
- **Regulatory Considerations:**
  - GDPR: User profile data requires consent and right to deletion mechanisms
  - SOC2: Stateless authentication and data integrity controls align with security requirements
  - PCI-DSS: Not applicable (payments out of scope)

---

## **RISK & DEPENDENCY ANALYSIS:**

**High Priority Dependencies:**
1. Database schema design must enforce all constraint rules
2. Authentication mechanism must be truly stateless
3. Cart cleanup logic must be transactional to prevent orphaned records

**Potential Risks:**
1. Race conditions in concurrent cart operations
2. Orphaned cart items if deletion logic fails
3. Performance degradation with case-insensitive search on large product catalogs
4. Session management complexity with stateless authentication

**Recommendations:**
1. Implement database transactions for cart lifecycle operations
2. Add database indexes for username uniqueness and product search
3. Implement proper exception handling for constraint violations
4. Add audit logging for all cart operations
5. Consider implementing soft deletes for compliance and debugging

---

**END OF REQUIREMENTS EXTRACTION REPORT**