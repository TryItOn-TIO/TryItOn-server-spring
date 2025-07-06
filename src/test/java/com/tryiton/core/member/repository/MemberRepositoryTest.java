package com.tryiton.core.member.repository;

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

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // given: 테스트용 Member 객체 생성
        testMember = Member.builder()
            .email("test@example.com")
            .username("테스트유저")
            .birthDate(LocalDate.of(2000, 1, 1))
            .gender(Gender.M)
            .phoneNum("010-1234-5678")
            .provider(AuthProvider.EMAIL)
            .role(UserRole.USER)
            .build();

        // when: TestEntityManager를 사용해 DB에 저장
        entityManager.persist(testMember);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("새로운 Member를 저장하고, 저장된 Member를 반환해야 한다")
    void save_ShouldPersistAndReturnMember() {
        // given
        Member newMember = Member.builder()
            .email("newbie@example.com")
            .username("새로운유저")
            .birthDate(LocalDate.of(1995, 1, 1))
            .gender(Gender.F)
            .phoneNum("010-8765-4321")
            .provider(AuthProvider.GOOGLE)
            .role(UserRole.USER)
            .build();

        // when
        Member savedMember = memberRepository.save(newMember);

        // then
        // findById로 DB에서 다시 조회하여 검증
        Member foundMember = entityManager.find(Member.class, savedMember.getId());
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getEmail()).isEqualTo("newbie@example.com");
    }


    @Test
    @DisplayName("ID로 Member를 조회하면, 올바른 Member를 포함한 Optional 객체를 반환해야 한다")
    void findById_WhenMemberExists_ShouldReturnOptionalOfMember() {
        // when
        Optional<Member> foundMemberOptional = memberRepository.findById(testMember.getId());

        // then
        assertThat(foundMemberOptional).isPresent(); // Optional이 비어있지 않은지 확인
        assertThat(foundMemberOptional.get().getId()).isEqualTo(testMember.getId());
        assertThat(foundMemberOptional.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면, 비어있는 Optional 객체를 반환해야 한다")
    void findById_WhenMemberDoesNotExist_ShouldReturnEmptyOptional() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<Member> foundMemberOptional = memberRepository.findById(nonExistentId);

        // then
        assertThat(foundMemberOptional).isNotPresent(); // Optional이 비어있는지 확인
    }

    @Test
    @DisplayName("Email로 Member를 조회하면, 올바른 Member를 포함한 Optional 객체를 반환해야 한다")
    void findByEmail_WhenMemberExists_ShouldReturnOptionalOfMember() {
        // when
        Optional<Member> foundMemberOptional = memberRepository.findByEmail("test@example.com");

        // then
        assertThat(foundMemberOptional).isPresent();
        assertThat(foundMemberOptional.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundMemberOptional.get().getUsername()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 Email로 조회하면, 비어있는 Optional 객체를 반환해야 한다")
    void findByEmail_WhenMemberDoesNotExist_ShouldReturnEmptyOptional() {
        // when
        Optional<Member> foundMemberOptional = memberRepository.findByEmail("nobody@example.com");

        // then
        assertThat(foundMemberOptional).isNotPresent();
    }
}