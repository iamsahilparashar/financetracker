package com.example.financetracker.repository;

import com.example.financetracker.entity.Transaction;
import com.example.financetracker.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // cursor based pagination
    Slice<Transaction> findByUserIdAndDatetimeBeforeOrderByDatetimeDesc(
            Long userId,
            LocalDateTime cursor,
            Pageable pageable
    );

    // analytics — summary
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.transactionType = :type
        AND t.datetime BETWEEN :from AND :to
    """)
    BigDecimal sumByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // analytics — category wise spending
    @Query("""
        SELECT c.name,
               SUM(t.amount * tc.splitPercentage / 100)
        FROM Transaction t
        JOIN t.categories tc
        JOIN tc.category c
        WHERE t.user.id = :userId
        AND t.transactionType = 'DEBIT'
        AND t.datetime BETWEEN :from AND :to
        GROUP BY c.id, c.name
        ORDER BY SUM(t.amount * tc.splitPercentage / 100) DESC
    """)
    List<Object[]> findCategoryWiseSpending(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // analytics — trends
    @Query("""
        SELECT c.name,
               FUNCTION('DATE_FORMAT', t.datetime, '%Y-%m'),
               SUM(t.amount * tc.splitPercentage / 100)
        FROM Transaction t
        JOIN t.categories tc
        JOIN tc.category c
        WHERE t.user.id = :userId
        AND t.transactionType = 'DEBIT'
        AND t.datetime BETWEEN :from AND :to
        GROUP BY c.id, c.name,
                 FUNCTION('DATE_FORMAT', t.datetime, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', t.datetime, '%Y-%m') ASC
    """)
    List<Object[]> findCategoryWiseSpendingByMonth(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}