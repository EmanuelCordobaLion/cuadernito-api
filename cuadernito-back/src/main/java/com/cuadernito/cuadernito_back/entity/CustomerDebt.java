package com.cuadernito.cuadernito_back.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_debts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "document_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDebt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(nullable = false, length = 100)
    private String customerFirstName;

    @Column(nullable = false, length = 100)
    private String customerLastName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DebtStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (remainingAmount == null) {
            remainingAmount = totalAmount;
        }
        if (status == null) {
            status = DebtStatus.PENDIENTE;
        }
    }

    public enum DebtStatus {
        PENDIENTE,
        PARCIAL,
        PAGADO
    }
}
