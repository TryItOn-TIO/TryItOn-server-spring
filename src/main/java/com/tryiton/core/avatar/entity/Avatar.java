package com.tryiton.core.avatar.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Avatar extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    private Long id;

    @Column(name = "pose_img", nullable = false)
    private String poseImg;

    @Column(name = "upper_mask_img", nullable = false)
    private String upperMaskImg;

    @Column(name = "lower_mask_img", nullable = false)
    private String lowerMaskImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(name = "avatar_img", nullable = false, length = 600)
    private String avatarImg;

    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvatarItem> items = new ArrayList<>();

    @Builder
    public Avatar(Long id, String tryOnImg, String poseImg, String upperMaskImg,
        String lowerMaskImg) {
        this.id = id;
        this.avatarImg = tryOnImg;
        this.poseImg = poseImg;
        this.upperMaskImg = upperMaskImg;
        this.lowerMaskImg = lowerMaskImg;
    }

    // 일 대 다 매핑
    public void setMappingUser(Member member) {
        this.member = member;
        member.getAvatars().add(this);
    }

    @Setter
    @Column(name = "is_bookmarked", nullable = false)
    private boolean isBookmarked;

    public void addItem(AvatarItem item) {
        items.add(item);
        item.setAvatar(this);
    }
}
