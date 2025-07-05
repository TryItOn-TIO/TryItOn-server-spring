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
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

//@ActiveProfiles("test")
//@DataJpaTest
//class WishlistRepositoryTest {
//
//    @Autowired
//    private WishlistRepository wishlistRepository;
//
//    @Autowired
//    private WishlistItemRepository wishlistItemRepository; // WishlistItem을 저장하기 위해 추가
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    private Member user1;
//    private Wishlist wishlist1;
//    private Product product1, product2;
//
//
//    @BeforeEach
//    void setUp() {
//        // given: 테스트 데이터 준비
//        user1 = Member.builder()
//            .email("wishlist-user1@example.com").username("user1")
//            .birthDate(LocalDate.now()).gender(Gender.M).phoneNum("010-1111-1111")
//            .provider(AuthProvider.EMAIL).role(UserRole.USER)
//            .build();
//        entityManager.persist(user1);
//
//        Member user2 = Member.builder()
//            .email("wishlist-user2@example.com").username("user2")
//            .birthDate(LocalDate.now()).gender(Gender.F).phoneNum("010-2222-2222")
//            .provider(AuthProvider.EMAIL).role(UserRole.USER)
//            .build();
//        entityManager.persist(user2);
//
//        Category category = new Category();
//        entityManager.persist(category);
//
//        product1 = Product.builder().productName("상품1").brand("A").price(100).img1("p1.jpg").category(category).build();
//        product2 = Product.builder().productName("상품2").brand("B").price(200).img1("p2.jpg").category(category).build();
//        entityManager.persist(product1);
//        entityManager.persist(product2);
//
//        // user1의 찜 목록 생성
//        wishlist1 = Wishlist.builder().user(user1).build();
//        entityManager.persist(wishlist1);
//
//        // user1의 찜 목록에 상품 추가
//        WishlistItem item1 = WishlistItem.builder().wishlist(wishlist1).product(product1).build();
//        WishlistItem item2 = WishlistItem.builder().wishlist(wishlist1).product(product2).build();
//        wishlistItemRepository.saveAll(List.of(item1, item2));
//
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//    @Test
//    @DisplayName("Member 객체로 Wishlist를 조회하면, 올바른 Wishlist를 포함한 Optional을 반환한다")
//    void findByUser_WhenWishlistExists_ShouldReturnOptionalOfWishlist() {
//        // when
//        Member persistedUser1 = entityManager.find(Member.class, user1.getId());
//        Optional<Wishlist> foundWishlistOpt = wishlistRepository.findByUser(persistedUser1);
//
//        // then
//        assertThat(foundWishlistOpt).isPresent();
//        assertThat(foundWishlistOpt.get().getWishlistId()).isEqualTo(wishlist1.getWishlistId());
//        assertThat(foundWishlistOpt.get().getUser().getId()).isEqualTo(user1.getId());
//    }
//
//    @Test
//    @DisplayName("사용자 ID로 Wishlist를 조회하면, 올바른 Wishlist를 포함한 Optional을 반환한다")
//    void findByUserId_WhenWishlistExists_ShouldReturnOptionalOfWishlist() {
//        // when
//        Optional<Wishlist> foundWishlistOpt = wishlistRepository.findByUserId(user1.getId());
//
//        // then
//        assertThat(foundWishlistOpt).isPresent();
//        assertThat(foundWishlistOpt.get().getWishlistId()).isEqualTo(wishlist1.getWishlistId());
//    }
//
//    @Test
//    @DisplayName("찜 목록이 없는 사용자 ID로 조회 시, 비어있는 Optional을 반환한다")
//    void findByUserId_WhenWishlistDoesNotExist_ShouldReturnEmptyOptional() {
//        // given
//        // user2는 찜 목록이 없습니다.
//        Long user2Id = entityManager.getEntityManager().createQuery("select m.id from Member m where m.email = :email", Long.class)
//            .setParameter("email", "wishlist-user2@example.com")
//            .getSingleResult();
//
//        // when
//        Optional<Wishlist> foundWishlistOpt = wishlistRepository.findByUserId(user2Id);
//
//        // then
//        assertThat(foundWishlistOpt).isNotPresent();
//    }
//
//    @Test
//    @DisplayName("사용자 ID로 찜한 상품들의 ID 목록을 정확히 반환해야 한다")
//    void findProductIdsByUserId_ShouldReturnCorrectProductIds() {
//        // when
//        List<Long> productIds = wishlistRepository.findProductIdsByUserId(user1.getId());
//
//        // then
//        assertThat(productIds).isNotNull();
//        assertThat(productIds).hasSize(2);
//        assertThat(productIds).containsExactlyInAnyOrder(product1.getId(), product2.getId());
//    }
//
//    @Test
//    @DisplayName("찜한 상품이 없는 사용자 ID로 조회 시, 비어있는 리스트를 반환해야 한다")
//    void findProductIdsByUserId_WhenNoItems_ShouldReturnEmptyList() {
//        // given
//        Member user3 = Member.builder()
//            .email("wishlist-user3@example.com").username("user3")
//            .birthDate(LocalDate.now()).gender(Gender.M).phoneNum("010-3333-3333")
//            .provider(AuthProvider.EMAIL).role(UserRole.USER)
//            .build();
//        entityManager.persist(user3);
//        entityManager.flush();
//
//        // when
//        List<Long> productIds = wishlistRepository.findProductIdsByUserId(user3.getId());
//
//        // then
//        assertThat(productIds).isNotNull();
//        assertThat(productIds).isEmpty();
//    }
//}