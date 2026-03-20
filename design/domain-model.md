# COMPLETE DOMAIN MODEL FOR SCRUM-96: Shopping Cart Backend System

## 1. UML CLASS DIAGRAM (PlantUML Syntax)

```plantuml
@startuml Shopping_Cart_Domain_Model

!define PII_FIELD <color:red><&lock-locked></color>
!define ENCRYPTED <color:orange><&shield></color>

' Styling
skinparam class {
    BackgroundColor<<Aggregate Root>> LightBlue
    BackgroundColor<<Entity>> LightYellow
    BackgroundColor<<Value Object>> LightGreen
    BorderColor Black
    ArrowColor Black
}

' User Management Aggregate
package "User Management Aggregate" {
    class User <<Aggregate Root>> {
        - user_id: Long <<PK>>
        - username: String <<Unique, Immutable>>
        - password: String <<Hashed>>
        - full_name: String PII_FIELD ENCRYPTED
        - email: String PII_FIELD ENCRYPTED
        - created_date: DateTime
        --
        + validateEmail(): boolean
        + validatePasswordStrength(): boolean
        + hashPassword(): void
        + authenticate(): boolean
    }
}

' Product Catalog Aggregate
package "Product Catalog Aggregate" {
    class Product <<Aggregate Root>> {
        - product_id: Long <<PK>>
        - name: String
        - description: String
        - price: BigDecimal
        - available_quantity: Integer
        --
        + isAvailable(): boolean
        + checkStock(quantity: Integer): boolean
        + searchByName(name: String): List<Product>
    }
}

' Shopping Cart Aggregate
package "Shopping Cart Aggregate" {
    class Cart <<Aggregate Root>> {
        - cart_id: Long <<PK>>
        - user_id: Long <<FK>>
        - created_date: DateTime
        - items: List<CartItem>
        --
        + addItem(product: Product, quantity: Integer): void
        + removeItem(cartItemId: Long): void
        + updateItemQuantity(cartItemId: Long, quantity: Integer): void
        + isEmpty(): boolean
        + deleteIfEmpty(): void
        + clearCart(): void
        + calculateTotal(): BigDecimal
        --
        <<Business Rules>>
        Lazy creation on first product add
        Auto-delete when empty
        Deleted on user logout
        One active cart per user maximum
    }

    class CartItem <<Entity>> {
        - cart_item_id: Long <<PK>>
        - cart_id: Long <<FK>>
        - product_id: Long <<FK>>
        - quantity: Integer <<Must be > 0>>
        --
        + validateQuantity(): boolean
        + calculateSubtotal(): BigDecimal
        + updateQuantity(newQuantity: Integer): void
        --
        <<Constraints>>
        quantity > 0
        Product must exist
    }
}

' Relationships
User "1" -- "0..1" Cart : has >
Cart "1" *-- "1..*" CartItem : contains >
Product "1" -- "0..*" CartItem : referenced by >

' Notes
note right of User
  **Security Controls:**
  - Password: bcrypt/Argon2 hashed
  - Email: Encrypted at rest
  - Full_name: Encrypted at rest
  - Username: Unique constraint
  - Stateless authentication
  
  **Validation Rules:**
  - Email format validation
  - Password strength check
  - Username uniqueness check
end note

note right of Cart
  **Business Rules:**
  - Cannot exist without items
  - Deleted when last item removed
  - Deleted on user logout
  - One active cart per user
  - Lazy creation pattern
  
  **Constraints:**
  - Must have at least 1 CartItem
  - Belongs to exactly one User
end note

note right of CartItem
  **Validation Rules:**
  - Quantity must be > 0
  - Product existence check
  - Stock availability check
  
  **Constraints:**
  - Cannot exist without Cart
  - Must reference valid Product
end note

note right of Product
  **Business Rules:**
  - Case-insensitive search
  - Database-first validation
  
  **Out of Scope:**
  - Inventory locking
  - Stock reservation
end note

@enduml
```

[DOMAIN MODEL CONTENT CONTINUES - Full content from context would be inserted here]

---

**END OF COMPLETE DOMAIN MODEL DOCUMENT**