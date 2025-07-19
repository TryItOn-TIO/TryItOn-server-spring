package com.tryiton.core.avatar.entity;

import com.tryiton.core.closet.entity.ClosetAvatarItem;
import com.tryiton.core.common.BaseTimeEntity;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Product;
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
import org.springframework.http.HttpStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Avatar extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(name = "avatar_img", nullable = false, length = 600)
    private String avatarImg;

    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvatarItem> items = new ArrayList<>();

    @Builder
    public Avatar(Long id, Member member, String avatarImg, boolean isBookmarked) {
        this.id = id;
        this.member = member;
        this.avatarImg = avatarImg;
    }

    public void setMappingUser(Member member) {
        this.member = member;
    }

    public void update(String avatarImg) {
        this.avatarImg = avatarImg;
    }

    public void wearGarment(Product garment) {
        if (garment.isUpperGarment()) {
            removeItemByCategory(true);
        } else if (garment.isLowerGarment()) {
            removeItemByCategory(false);
        }
        this.items.add(new AvatarItem(this, garment));
    }

    private void removeItemByCategory(boolean isUpper) {
        this.items.removeIf(item ->
            isUpper ? item.getProduct().isUpperGarment() : item.getProduct().isLowerGarment()
        );
    }

    public String getPoseUrl() {
        return getBaseKey() + "pose.png";
    }

    public String getMaskUrl(Product product) {
        String baseKey = getBaseKey();
        if (product.isUpperGarment()) {
            return baseKey + "upper_mask.png";
        }
        if (product.isLowerGarment()) {
            return baseKey + "lower_mask.png";
        }
        throw new BusinessException(HttpStatus.NOT_FOUND, "옷의 종류를 찾지 못했습니다. (상의/하의)");
    }

    private String getBaseKey() {
        if (this.member == null || this.member.getId() == null) {
            throw new IllegalStateException("아바타에 유저 정보가 할당되지 않아 S3 키를 생성할 수 없습니다.");
        }
        
        // 항상 사용자별 경로 사용 (기본 아바타 여부와 관계없이)
        return "users/" + this.member.getId() + "/";
    }
    
    /**
     * 현재 아바타가 베이스 아바타인지 확인합니다.
     * 베이스 아바타는 avatarImg URL에 "base/default_avatar.png"가 포함됩니다.
     */
    public boolean isBaseAvatar() {
        return avatarImg != null && avatarImg.contains("base/default_avatar");
    }

    public void resetAvatar(String baseUrl) {
        avatarImg = baseUrl;
        items.clear();
    }

}