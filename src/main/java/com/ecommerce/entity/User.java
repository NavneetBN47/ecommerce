package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Entity - Maps to users table
 * Represents system users with authentication and profile information
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50, updatable = false)
    private String username;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * Computed property for full name (LLD compatibility)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Set full name by splitting into first and last names
     */
    @Transient
    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        }
    }
}