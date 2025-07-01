package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Gender;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class AvatarRepositoryTest {

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member user;
    private Avatar avatar1, avatar2, avatar3, latestAvatar;

    @BeforeEach
    void setUp() throws InterruptedException {
        // given: 테스트용 Member 및 Avatar 객체 생성 및 저장
        user = Member.builder()
            .email("avatar-test@example.com").username("avatarUser")
            .birthDate(LocalDate.now()).gender(Gender.M).phoneNum("010-1234-1234")
            .provider(AuthProvider.EMAIL).role(UserRole.USER)
            .build();
        entityManager.persist(user);

        // 아바타 생성 (생성 시간 순서 보장)
        avatar1 = createAvatar("avatar1.jpg", user, true); // 북마크 O
        Thread.sleep(10);
        avatar2 = createAvatar("avatar2.jpg", user, false); // 북마크 X
        Thread.sleep(10);
        avatar3 = createAvatar("avatar3.jpg", user, true); // 북마크 O
        Thread.sleep(10);
        latestAvatar = createAvatar("latest.jpg", user, false); // 북마크 X, 가장 최신

        entityManager.flush();
        entityManager.clear();
    }

    // 테스트 데이터 생성을 위한 헬퍼 메서드
    private Avatar createAvatar(String imgUrl, Member member, boolean isBookmarked) {
        Avatar avatar = Avatar.builder()
            .avatarImg(imgUrl)
            .poseImg("pose.jpg")
            .upperMaskImg("upper.jpg")
            .lowerMaskImg("lower.jpg")
            .isBookmarked(isBookmarked)
            .member(member)
            .build();
        return entityManager.persist(avatar);
    }

    @Test
    @DisplayName("특정 사용자의 북마크된 착장 목록을 최신순으로 조회해야 한다")
    void findAllByMemberAndIsBookmarkedTrueOrderByCreatedAtDesc_ShouldReturnBookmarkedAvatars() {
        // when
        Member persistedUser = entityManager.find(Member.class, user.getId());
        List<Avatar> bookmarkedAvatars = avatarRepository.findAllByMemberAndIsBookmarkedTrueOrderByCreatedAtDesc(persistedUser);

        // then
        assertThat(bookmarkedAvatars).hasSize(2);
        // 최신순 정렬 확인 (avatar3가 avatar1보다 나중에 생성됨)
        assertThat(bookmarkedAvatars.get(0).getId()).isEqualTo(avatar3.getId());
        assertThat(bookmarkedAvatars.get(1).getId()).isEqualTo(avatar1.getId());
        // 모든 결과가 북마크된 상태인지 확인
        assertThat(bookmarkedAvatars).allMatch(Avatar::isBookmarked);
    }

    @Test
    @DisplayName("특정 사용자의 가장 최근에 입혀본 착장 1개를 조회해야 한다")
    void findTopByMemberIdOrderByCreatedAtDesc_ShouldReturnLatestAvatar() {
        // when
        // @Query("... a.member.id = :userId ...")는 Long 타입의 userId를 파라미터로 받습니다.
        Avatar foundAvatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(user.getId());

        // then
        assertThat(foundAvatar).isNotNull();
        assertThat(foundAvatar.getId()).isEqualTo(latestAvatar.getId());
        assertThat(foundAvatar.getAvatarImg()).isEqualTo("latest.jpg");
    }

    @Test
    @DisplayName("착장 ID와 사용자 정보로 정확한 착장 1개를 조회해야 한다")
    void findByIdAndMember_WhenOwnerMatches_ShouldReturnAvatar() {
        // when
        Member persistedUser = entityManager.find(Member.class, user.getId());
        Optional<Avatar> foundAvatarOpt = avatarRepository.findByIdAndMember(avatar1.getId(), persistedUser);

        // then
        assertThat(foundAvatarOpt).isPresent();
        assertThat(foundAvatarOpt.get().getId()).isEqualTo(avatar1.getId());
    }

    @Test
    @DisplayName("착장 ID가 존재하더라도 사용자 정보가 다르면 조회가 안돼야 한다")
    void findByIdAndMember_WhenOwnerDoesNotMatch_ShouldReturnEmpty() {
        // given
        // 다른 사용자 생성
        Member anotherUser = Member.builder()
            .email("another@example.com").username("anotherUser")
            .birthDate(LocalDate.now()).gender(Gender.F).phoneNum("010-9876-5432")
            .provider(AuthProvider.EMAIL).role(UserRole.USER)
            .build();
        entityManager.persist(anotherUser);
        entityManager.flush();

        // when
        // avatar1은 user의 것이지만, anotherUser로 조회 시도
        Optional<Avatar> foundAvatarOpt = avatarRepository.findByIdAndMember(avatar1.getId(), anotherUser);

        // then
        assertThat(foundAvatarOpt).isNotPresent();
    }
}
