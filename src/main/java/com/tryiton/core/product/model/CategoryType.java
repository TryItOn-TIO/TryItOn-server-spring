package com.tryiton.core.product.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    TOP("상의", 1L),
    OUTER("아우터", 2L),
    BOTTOM("하의", 3L),
    DRESS("원피스/스커트", 4L),
    SHOES("신발", 5L),
    ACCESSORY("소품/ACC", 6L);

    private final String name;
    private final Long parentCategoryId;

    public static CategoryType fromName(String name) {
        return Arrays.stream(values())
            .filter(type -> type.name.equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 이름입니다: " + name));
    }
}
