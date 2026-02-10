package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CartHistory entity for audit trail of cart operations
 */
@Entity
@Table(name = "cart_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "history_id", updatable = false, nullable = false)
    private UUID historyId;

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(name = "action_details", columnDefinition = "jsonb")
    private String actionDetails;

    @Column(name = "performed_by")
    private UUID performedBy;

    @CreatedDate
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", insertable = false, updatable = false)
    private User performedByUser;

    public enum ActionType {
        CREATED, ITEM_ADDED, ITEM_REMOVED, ITEM_UPDATED, STATUS_CHANGED, EXPIRED
    }
}