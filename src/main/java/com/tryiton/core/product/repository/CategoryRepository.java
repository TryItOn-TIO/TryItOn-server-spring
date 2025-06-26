package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 상위 카테고리 ID로 하위 카테고리 조회
    List<Category> findByParentId(Integer parentId);
}
