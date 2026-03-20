package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_lineage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Data lineage tracking for compliance")
public class DataLineage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Entity type", example = "Patient", required = true)
    private String entityType;

    @Column(nullable = false)
    @Schema(description = "Entity ID", example = "123", required = true)
    private Long entityId;

    @Column(nullable = false, length = 50)
    @Schema(description = "Operation type", example = "CREATE", required = true)
    private String operation;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Previous value (JSON)")
    private String previousValue;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "New value (JSON)")
    private String newValue;

    @Column(length = 100)
    @Schema(description = "User who performed the operation", example = "admin@healthcare.com")
    private String performedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp of the operation", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
}