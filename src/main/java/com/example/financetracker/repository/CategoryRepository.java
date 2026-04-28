package com.example.financetracker.repository;

import com.example.financetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository
    extends JpaRepository<Category, Long> {

    // only fetch active categories
    List<Category> findByUserIdAndIsActiveTrue(Long userId);

    // check duplicate only among active categories
    boolean existsByNameAndUserIdAndIsActiveTrue(
        String name, Long userId);

    // find active category by id
    Optional<Category> findByIdAndIsActiveTrue(Long id);
}