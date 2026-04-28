package com.example.financetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_category",
    uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {"transaction_id", "category_id"}
            )
        },
    indexes = {
            @Index(name = "idx_transaction_category_transaction_id", columnList = "transaction_id"),
            @Index(name = "idx_transaction_category_category_id", columnList = "category_id")
        }
    )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "split_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal splitPercentage;
}
