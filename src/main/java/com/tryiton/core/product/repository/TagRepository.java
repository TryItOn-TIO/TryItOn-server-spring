package com.tryiton.core.product.repository;

import com.tryiton.core.product.dto.TagScoreDto;
import com.tryiton.core.product.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // ★★★ 타입 불일치 오류를 해결하기 위해 CAST 함수를 추가한 최종 버전입니다. ★★★
    @Query(value = """
            SELECT
                t.tag_id AS tagId,
                CAST(SUM(w.weight) AS DOUBLE) AS score
            FROM (
                SELECT pt.tag_id, 3 AS weight FROM orders o JOIN order_item oi ON o.order_id = oi.order_id JOIN product_variant pv ON oi.variant_id = pv.variant_id JOIN product p ON pv.product_id = p.product_id JOIN product_tag pt ON p.product_id = pt.product_id WHERE o.user_id = :userId
                UNION ALL
                SELECT pt.tag_id, 2 AS weight FROM wishlist w JOIN wishlist_item wi ON w.wishlist_id = wi.wishlist_id JOIN product_tag pt ON wi.product_id = pt.product_id WHERE w.user_id = :userId
            ) AS w JOIN tag t ON w.tag_id = t.tag_id GROUP BY t.tag_id
        """, nativeQuery = true)
    List<TagScoreDto> findUserFavoriteTags(@Param("userId") Long userId);
}
