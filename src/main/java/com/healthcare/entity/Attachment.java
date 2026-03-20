package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "File attachment for medical records")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the attachment", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @Schema(description = "Associated medical record")
    private MedicalRecord medicalRecord;

    @NotBlank(message = "File name is required")
    @Column(nullable = false, length = 255)
    @Schema(description = "Original file name", example = "lab_results.pdf", required = true)
    private String fileName;

    @NotBlank(message = "File type is required")
    @Column(nullable = false, length = 100)
    @Schema(description = "MIME type of the file", example = "application/pdf", required = true)
    private String fileType;

    @Column(nullable = false)
    @Schema(description = "File size in bytes", example = "1024000", required = true)
    private Long fileSize;

    @NotBlank(message = "Storage path is required")
    @Column(nullable = false, length = 500)
    @Schema(description = "Storage location or S3 key", example = "s3://bucket/path/file.pdf", required = true)
    private String storagePath;

    @Column(nullable = false)
    @Schema(description = "Encryption status", example = "true")
    private Boolean encrypted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when attachment was uploaded", example = "2024-01-15T10:30:00")
    private LocalDateTime uploadedAt;

    @Column(length = 100)
    @Schema(description = "User who uploaded the file", example = "Dr. Smith")
    private String uploadedBy;
}