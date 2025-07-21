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

    @Query(value = "WITH RECURSIVE category_tree AS ( " +
            "  SELECT category_id, parent_category_id FROM category WHERE category_id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.category_id, c.parent_category_id FROM category c JOIN category_tree ct ON c.parent_category_id = ct.category_id " +
            ") SELECT category_id FROM category_tree", nativeQuery = true)
    List<Long> findAllChildrenIdsByParentId(@Param("categoryId") Long categoryId);
}