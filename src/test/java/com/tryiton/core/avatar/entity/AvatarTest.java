package com.tryiton.core.avatar.entity;

import com.tryiton.core.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarTest {

    private Avatar avatar;

    // Mocks are still declared at the class level
    @Mock
    private Product oldTop, newTop, bottom, shoes;

    @BeforeEach
    void setUp() {
        // Only initialize the object under test here
        avatar = new Avatar();
    }

    @Test
    @DisplayName("아무것도 안 입은 상태에서 상의를 입으면, 착용 아이템은 1개가 되어야 한다")
    void wearGarment_whenNoClothes_thenWearTop() {
        // given: Define behavior only needed for this test
        when(newTop.isUpperGarment()).thenReturn(true);

        // when
        avatar.wearGarment(newTop);

        // then
        assertThat(avatar.getItems()).hasSize(1);
        assertThat(avatar.getItems().get(0).getProduct()).isEqualTo(newTop);
    }

    @Test
    @DisplayName("상의를 입은 상태에서 새로운 상의를 입으면, 기존 상의는 벗고 새 상의만 입어야 한다")
    void wearGarment_whenWearingTop_thenChangeTop() {
        // given: Define behavior for this specific scenario
        when(oldTop.isUpperGarment()).thenReturn(true);
        when(newTop.isUpperGarment()).thenReturn(true);
        when(newTop.getProductName()).thenReturn("새로운 상의");

        avatar.wearGarment(oldTop); // First, wear the old top
        assertThat(avatar.getItems()).hasSize(1);

        // when
        avatar.wearGarment(newTop); // Now, wear the new top

        // then
        assertThat(avatar.getItems()).hasSize(1);
        assertThat(avatar.getItems().get(0).getProduct().getProductName()).isEqualTo("새로운 상의");
    }

    @Test
    @DisplayName("상의를 입은 상태에서 하의를 입으면, 상의와 하의 모두 착용해야 한다")
    void wearGarment_whenWearingTop_thenWearBottom() {
        // given
        when(oldTop.isUpperGarment()).thenReturn(true);
        when(oldTop.getProductName()).thenReturn("기존 상의");
        when(bottom.isLowerGarment()).thenReturn(true);
        when(bottom.getProductName()).thenReturn("하의");

        avatar.wearGarment(oldTop);

        // when
        avatar.wearGarment(bottom);

        // then
        assertThat(avatar.getItems()).hasSize(2);
        assertThat(avatar.getItems()).extracting(item -> item.getProduct().getProductName())
            .containsExactlyInAnyOrder("기존 상의", "하의");
    }

    @Test
    @DisplayName("상의와 하의를 입은 상태에서 신발을 신으면, 3개의 아이템 모두 착용해야 한다")
    void wearGarment_whenWearingTopAndBottom_thenWearShoes() {
        // given
        when(oldTop.isUpperGarment()).thenReturn(true);
        when(oldTop.getProductName()).thenReturn("기존 상의");
        when(bottom.isLowerGarment()).thenReturn(true);
        when(bottom.getProductName()).thenReturn("하의");
        // For shoes, define both as false
        when(shoes.isUpperGarment()).thenReturn(false);
        when(shoes.isLowerGarment()).thenReturn(false);
        when(shoes.getProductName()).thenReturn("신발");

        avatar.wearGarment(oldTop);
        avatar.wearGarment(bottom);

        // when
        avatar.wearGarment(shoes);

        // then
        assertThat(avatar.getItems()).hasSize(3);
        assertThat(avatar.getItems()).extracting(item -> item.getProduct().getProductName())
            .containsExactlyInAnyOrder("기존 상의", "하의", "신발");
    }
}