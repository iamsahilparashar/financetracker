package com.example.financetracker.repository;

import java.util.List;

import com.example.financetracker.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, Long> {

    List<TransactionCategory> findByTransactionId(Long transactionId);

    @Query("SELECT tc FROM TransactionCategory tc " +
        "WHERE tc.transaction.id IN :transactionIds")
    List<TransactionCategory> findByTransactionIdIn(
        @Param("transactionIds") List<Long> transactionIds
    );

    void deleteByTransactionId(Long transactionId);
}
