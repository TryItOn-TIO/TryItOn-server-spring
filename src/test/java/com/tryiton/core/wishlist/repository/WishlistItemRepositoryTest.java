package com.tryiton.core.wishlist.repository;

import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Gender;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class WishlistItemRepositoryTest {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Wishlist wishlist;

    @BeforeEach
    void setUp() throws InterruptedException {
        // given: Member 엔티티의 모든 필수 필드를 채워서 생성
        Member user = Member.builder()
            .email("wishlist-test@example.com")
            .username("wishlist-user")
            .birthDate(LocalDate.of(2000, 1, 1))
            .gender(Gender.F) // nullable=false 필드
            .phoneNum("010-1234-5678") // nullable=false 필드
            .provider(AuthProvider.EMAIL) // nullable=false 필드
            .role(UserRole.USER)
            .build();
        entityManager.persist(user);

        wishlist = Wishlist.builder().user(user).build();
        entityManager.persist(wishlist);

        Category category = new Category();
        entityManager.persist(category);

        Product product1 = Product.builder().productName("상품1").brand("TIO").price(100).img1("1.jpg").category(category).build();
        Product product2 = Product.builder().productName("상품2").brand("TIO").price(200).img1("2.jpg").category(category).build();
        Product product3 = Product.builder().productName("상품3").brand("TIO").price(300).img1("3.jpg").category(category).build();
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);

        WishlistItem item1 = WishlistItem.builder().wishlist(wishlist).product(product1).build();
        entityManager.persist(item1);
        entityManager.flush();
        Thread.sleep(10);

        WishlistItem item2 = WishlistItem.builder().wishlist(wishlist).product(product2).build();
        entityManager.persist(item2);
        entityManager.flush();
        Thread.sleep(10);

        WishlistItem item3 = WishlistItem.builder().wishlist(wishlist).product(product3).build();
        entityManager.persist(item3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("특정 찜 목록 ID로 아이템들을 조회할 때, 최신순(createdAt 내림차순)으로 정렬되어야 한다")
    void findAllByWishlist_WishlistIdOrderByCreatedAtDesc_ShouldReturnSortedItems() {
        // when
        List<WishlistItem> sortedItems = wishlistItemRepository.findAllByWishlist_WishlistIdOrderByCreatedAtDesc(wishlist.getWishlistId());

        // then
        assertThat(sortedItems).hasSize(3);
        assertThat(sortedItems.get(0).getProduct().getProductName()).isEqualTo("상품3");
        assertThat(sortedItems.get(2).getProduct().getProductName()).isEqualTo("상품1");
        assertThat(sortedItems).extracting(item -> item.getProduct().getProductName())
            .containsExactly("상품3", "상품2", "상품1");
    }

    @Test
    @DisplayName("아이템이 없는 찜 목록 ID로 조회 시, 비어있는 리스트를 반환해야 한다")
    void findAllByWishlist_WishlistIdOrderByCreatedAtDesc_WhenEmpty_ShouldReturnEmptyList() {
        // given
        Wishlist emptyWishlist = Wishlist.builder().build();
        entityManager.persist(emptyWishlist);
        entityManager.flush();

        // when
        List<WishlistItem> sortedItems = wishlistItemRepository.findAllByWishlist_WishlistIdOrderByCreatedAtDesc(emptyWishlist.getWishlistId());

        // then
        assertThat(sortedItems).isNotNull().isEmpty();
    }
}