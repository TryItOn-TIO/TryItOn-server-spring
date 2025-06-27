package com.tryiton.core.product.entity;


import jakarta.persistence.*;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id") // DB 컬럼명과 일치시킵니다.
    private Long id;

    private String categoryName;

    // --- 이 아래 코드들을 Category 클래스 내부에 추가하세요 ---

    // 부모 카테고리 (자신)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    // 자식 카테고리 목록 (자신)
    @OneToMany(mappedBy = "parentCategory")
    private List<Category> children = new ArrayList<>();
}