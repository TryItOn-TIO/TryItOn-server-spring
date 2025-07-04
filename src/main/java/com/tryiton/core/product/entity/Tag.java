package com.tryiton.core.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED) 왜 프로텍티드죠?
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String tagName;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    @Builder
    public Tag(Long id, String tagName, Set<Product> products) {
        this.id = id;
        this.tagName = tagName;
        this.products = products;
    }
}