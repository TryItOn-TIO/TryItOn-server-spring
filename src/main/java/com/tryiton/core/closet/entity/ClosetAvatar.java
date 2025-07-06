package com.tryiton.core.closet.entity;

import com.tryiton.core.common.BaseTimeEntity;
import com.tryiton.core.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosetAvatar extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closet_avatar_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @Column(name = "avatar_img", nullable = false, length = 600)
    private String avatarImage;

    @OneToMany(mappedBy = "closetAvatar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosetAvatarItem> items = new ArrayList<>();

    @Builder
    public ClosetAvatar(Member user, String avatarImage) {
        this.user = user;
        this.avatarImage = avatarImage;
    }

    public void addItem(ClosetAvatarItem item) {
        if (isFull()) {
            throw new IllegalStateException("옷장에는 최대 10개까지만 저장할 수 있습니다.");
        }
        this.items.add(item);
        item.setClosetAvatar(this);
    }

    private boolean isFull() {
        return this.items.size() >= 10;
    }

    /* 착장 전체의 조합이 같은지를 비교 */
    public boolean hasSameCombination(List<ClosetAvatarItem> otherItems) {
        if (otherItems == null || this.items.size() != otherItems.size()) {
            return false;
        }

        return this.items.stream()
            .allMatch(item -> otherItems.stream()
                .anyMatch(o -> o.getProduct().getId().equals(item.getProduct().getId())));
    }
}
