package com.tryiton.core.closet.service;

import com.tryiton.core.closet.dto.ClosetAvatarItemRequestDto;
import com.tryiton.core.closet.dto.ClosetAvatarResponseDto;
import com.tryiton.core.closet.dto.ClosetAvatarSaveRequestDto;
import com.tryiton.core.closet.entity.ClosetAvatar;
import com.tryiton.core.closet.entity.ClosetAvatarItem;
import com.tryiton.core.closet.repository.ClosetAvatarRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosetAvatarServiceTest {

    @InjectMocks
    private ClosetAvatarService closetAvatarService;

    @Mock
    private ClosetAvatarRepository closetAvatarRepository;

    @Mock
    private ProductRepository productRepository;

    private Member user;
    private Product product1, product2;
    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        user = Member.builder().id(1L).username("testUser").build();

        // --- 👇 여기부터 수정 ---
        // 테스트용 카테고리 생성
        parentCategory = Category.builder().id(1L).categoryName("의류").build();
        childCategory = Category.builder().id(2L).categoryName("상의").parentCategory(parentCategory).build();

        // Product 생성 시 Category 주입
        product1 = Product.builder().id(101L).productName("반팔 티셔츠").category(childCategory).build();
        product2 = Product.builder().id(102L).productName("긴팔 티셔츠").category(childCategory).build();
        // --- 👆 여기까지 수정 ---
    }

    @Test
    @DisplayName("착장 저장 성공")
    void saveClosetAvatar_Success() {
        // given
        ClosetAvatarSaveRequestDto requestDto = createSaveRequest("avatar.jpg", 101L, 102L);

        given(closetAvatarRepository.countByUserId(user.getId())).willReturn(0L);
        given(closetAvatarRepository.findWithItemsByUserId(user.getId())).willReturn(Collections.emptyList());
        given(productRepository.findById(101L)).willReturn(Optional.of(product1));
        given(productRepository.findById(102L)).willReturn(Optional.of(product2));

        // when
        closetAvatarService.saveClosetAvatar(user, requestDto);

        // then
        verify(closetAvatarRepository, times(1)).save(any(ClosetAvatar.class));
    }

    @Test
    @DisplayName("착장 저장 실패 - 10개 초과")
    void saveClosetAvatar_Fail_MaxLimitExceeded() {
        // given
        ClosetAvatarSaveRequestDto requestDto = createSaveRequest("avatar.jpg", 101L);
        given(closetAvatarRepository.countByUserId(user.getId())).willReturn(10L);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            closetAvatarService.saveClosetAvatar(user, requestDto);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("최대 10개의 착장만 저장할 수 있습니다.");
        verify(closetAvatarRepository, never()).save(any(ClosetAvatar.class));
    }

    @Test
    @DisplayName("착장 저장 실패 - 중복된 착장")
    void saveClosetAvatar_Fail_DuplicateCombination() {
        // given
        ClosetAvatarSaveRequestDto requestDto = createSaveRequest("new_avatar.jpg", 101L, 102L);
        ClosetAvatar existingAvatar = createClosetAvatar(user, "old_avatar.jpg", product1, product2);

        given(closetAvatarRepository.countByUserId(user.getId())).willReturn(1L);
        given(closetAvatarRepository.findWithItemsByUserId(user.getId())).willReturn(List.of(existingAvatar));
        given(productRepository.findById(101L)).willReturn(Optional.of(product1));
        given(productRepository.findById(102L)).willReturn(Optional.of(product2));


        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            closetAvatarService.saveClosetAvatar(user, requestDto);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).isEqualTo("이미 동일한 착장이 저장되어 있습니다.");
        verify(closetAvatarRepository, never()).save(any(ClosetAvatar.class));
    }

    @Test
    @DisplayName("착장 목록 조회 성공")
    void getClosetAvatarByUser_Success() {
        // given
        ClosetAvatar avatar1 = createClosetAvatar(user, "avatar1.jpg", product1);
        ClosetAvatar avatar2 = createClosetAvatar(user, "avatar2.jpg", product2);
        given(closetAvatarRepository.findWithItemsByUserId(user.getId())).willReturn(List.of(avatar1, avatar2));

        // when
        List<ClosetAvatarResponseDto> response = closetAvatarService.getClosetAvatarByUser(user.getId());

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getAvatarImage()).isEqualTo("avatar1.jpg");
        assertThat(response.get(1).getItemsByCategory()).hasSize(1);
    }

    @Test
    @DisplayName("착장 삭제 성공")
    void deleteClosetAvatar_Success() {
        // given
        Long avatarIdToDelete = 1L;
        ClosetAvatar avatar = ClosetAvatar.builder().id(avatarIdToDelete).user(user).build();
        given(closetAvatarRepository.findByIdAndUserId(avatarIdToDelete, user.getId())).willReturn(Optional.of(avatar));

        // when
        closetAvatarService.deleteClosetAvatar(avatarIdToDelete, user.getId());

        // then
        verify(closetAvatarRepository, times(1)).delete(avatar);
    }

    @Test
    @DisplayName("착장 삭제 실패 - 존재하지 않거나 권한 없음")
    void deleteClosetAvatar_Fail_NotFoundOrNoPermission() {
        // given
        Long nonExistentAvatarId = 99L;
        given(closetAvatarRepository.findByIdAndUserId(nonExistentAvatarId, user.getId())).willReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            closetAvatarService.deleteClosetAvatar(nonExistentAvatarId, user.getId());
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("해당 착장을 찾을 수 없거나 권한이 없습니다.");
        verify(closetAvatarRepository, never()).delete(any(ClosetAvatar.class));
    }


    // --- Helper Methods ---

    private ClosetAvatarSaveRequestDto createSaveRequest(String avatarImage, Long... productIds) {
        ClosetAvatarSaveRequestDto requestDto = new ClosetAvatarSaveRequestDto();

        List<ClosetAvatarItemRequestDto> items = Arrays.stream(productIds) // 이 부분을 수정했습니다.
            .map(this::createItemRequest)
            .collect(Collectors.toList());

        // Setter가 없으므로 ReflectionTestUtils를 사용해 필드에 값을 주입합니다.
        org.springframework.test.util.ReflectionTestUtils.setField(requestDto, "avatarImage", avatarImage);
        org.springframework.test.util.ReflectionTestUtils.setField(requestDto, "items", items);

        return requestDto;
    }

    private ClosetAvatarItemRequestDto createItemRequest(Long productId) {
        ClosetAvatarItemRequestDto itemDto = new ClosetAvatarItemRequestDto();
        org.springframework.test.util.ReflectionTestUtils.setField(itemDto, "productId", productId);
        return itemDto;
    }

    private ClosetAvatar createClosetAvatar(Member user, String avatarImage, Product... products) {
        ClosetAvatar avatar = ClosetAvatar.builder()
            .user(user)
            .avatarImage(avatarImage)
            .build();
        for (Product product : products) {
            ClosetAvatarItem item = ClosetAvatarItem.builder().product(product).build();
            avatar.addItem(item);
        }
        return avatar;
    }
}