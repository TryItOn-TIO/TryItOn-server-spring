package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long categoryId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :categoryId")
    Optional<Category> findByIdWithChildren(@Param("categoryId") Long categoryId);
}