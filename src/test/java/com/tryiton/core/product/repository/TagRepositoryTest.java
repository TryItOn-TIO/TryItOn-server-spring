package com.tryiton.core.product.repository;

import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Gender;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.TagScoreDto;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.entity.Tag;
import com.tryiton.core.wishlist.entity.Wishlist;
import com.tryiton.core.wishlist.entity.WishlistItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member user;
    private Product productA, productB, productC;
    private Tag tagCasual, tagStreet, tagFormal;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        user = Member.builder()
            .email("tag-test@example.com").username("tagUser")
            .birthDate(LocalDate.now()).gender(Gender.F).phoneNum("010-0000-0000")
            .provider(AuthProvider.EMAIL).role(UserRole.USER)
            .build();
        entityManager.persist(user);

        // 태그 생성
        tagCasual = Tag.builder().tagName("캐주얼").build();
        tagStreet = Tag.builder().tagName("스트릿").build();
        tagFormal = Tag.builder().tagName("포멀").build();

        entityManager.persist(tagCasual);
        entityManager.persist(tagStreet);
        entityManager.persist(tagFormal);

        // 상품 생성
        Category category = new Category();
        entityManager.persist(category);

        productA = Product.builder().productName("상품A").brand("A").price(100).img1("a.jpg").category(category).build();
        productB = Product.builder().productName("상품B").brand("B").price(200).img1("b.jpg").category(category).build();
        productC = Product.builder().productName("상품C").brand("C").price(300).img1("c.jpg").category(category).build();
        entityManager.persist(productA);
        entityManager.persist(productB);
        entityManager.persist(productC);

        // 상품-태그 연결 (product_tag 테이블)
        // 상품A: 캐주얼, 스트릿
        // 상품B: 스트릿, 포멀
        // 상품C: 캐주얼
        insertProductTag(productA.getId(), tagCasual.getId());
        insertProductTag(productA.getId(), tagStreet.getId());
        insertProductTag(productB.getId(), tagStreet.getId());
        insertProductTag(productB.getId(), tagFormal.getId());
        insertProductTag(productC.getId(), tagCasual.getId());

        // 구매 기록 생성 (3점) - 현재 Order 엔티티가 없으므로 주석 처리
        // 사용자(user)가 상품A(캐주얼, 스트릿)를 구매함.

        // 찜 기록 생성 (2점)
        // 사용자(user)가 상품B(스트릿, 포멀)와 상품C(캐주얼)를 찜함.
        Wishlist wishlist = Wishlist.builder().user(user).build();
        entityManager.persist(wishlist);

        WishlistItem wishlistItemB = WishlistItem.builder().wishlist(wishlist).product(productB).build();
        WishlistItem wishlistItemC = WishlistItem.builder().wishlist(wishlist).product(productC).build();
        entityManager.persist(wishlistItemB);
        entityManager.persist(wishlistItemC);

        entityManager.flush();
        entityManager.clear();
    }

    // 테스트 편의를 위한 헬퍼 메서드
    private void insertProductTag(Long productId, Long tagId) {
        entityManager.getEntityManager().createNativeQuery("INSERT INTO product_tag (product_id, tag_id) VALUES (?, ?)")
            .setParameter(1, productId)
            .setParameter(2, tagId)
            .executeUpdate();
    }

    // todo: 오더 테이블 생성 후 테스트가 통과되어야함

    @Test
    @DisplayName("사용자의 찜 기록을 기반으로 선호 태그 점수를 정확히 계산해야 한다")
    void findUserFavoriteTags_BasedOnWishlist_ShouldCalculateScoresCorrectly() {
        // given
        // setUp에서 찜 기록 생성: 상품B(스트릿, 포멀), 상품C(캐주얼)
        // 예상 점수:
        // 캐주얼: 2점 (상품C)
        // 스트릿: 2점 (상품B)
        // 포멀: 2점 (상품B)

        // when
        List<TagScoreDto> favoriteTags = tagRepository.findUserFavoriteTags(user.getId());

        // then
        assertThat(favoriteTags).hasSize(3);

        // 검증을 용이하게 하기 위해 List를 Map으로 변환
        Map<Long, Double> scoreMap = favoriteTags.stream()
            .collect(Collectors.toMap(TagScoreDto::getTagId, TagScoreDto::getScore));

        assertThat(scoreMap.get(tagCasual.getId())).isEqualTo(2.0);
        assertThat(scoreMap.get(tagStreet.getId())).isEqualTo(2.0);
        assertThat(scoreMap.get(tagFormal.getId())).isEqualTo(2.0);
    }

    // @Test
    // @DisplayName("사용자의 구매와 찜 기록을 모두 합산하여 선호 태그 점수를 정확히 계산해야 한다")
    // void findUserFavoriteTags_WithPurchaseAndWishlist_ShouldCalculateScoresCorrectly() {
    //     // given: 구매 기록 추가
    //     // Order, OrderItem, ProductVariant 엔티티가 필요합니다.
    //
    //     // 예상 점수:
    //     // 캐주얼: 3점(구매) + 2점(찜) = 5점
    //     // 스트릿: 3점(구매) + 2점(찜) = 5점
    //     // 포멀: 2점(찜) = 2점
    // }
}