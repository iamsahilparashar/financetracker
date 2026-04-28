package com.example.financetracker.repository;

import com.example.financetracker.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, Long> {

    List<TransactionCategory> findByTransactionId(Long transactionId);

    void deleteByTransactionId(Long transactionId);
}
