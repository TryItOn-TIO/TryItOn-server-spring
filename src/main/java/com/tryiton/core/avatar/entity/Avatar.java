package com.tryiton.core.avatar.entity;

import com.tryiton.core.common.BaseTimeEntity;
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
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Avatar extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    private Long id;

//    @Column(name = "pose_img", nullable = false)
//    private String poseImg;
//
//    @Column(name = "upper_mask_img", nullable = false)
//    private String upperMaskImg;
//
//    @Column(name = "lower_mask_img", nullable = false)
//    private String lowerMaskImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(name = "avatar_img", nullable = false, length = 600)
    private String avatarImg;

    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvatarItem> items = new ArrayList<>();

//    @Column(nullable = false)
//    private boolean isBookmarked = false; // 기본값은 false

    @Builder
    public Avatar(Long id, Member member, String avatarImg, boolean isBookmarked) {
        this.id = id;
        this.member = member;
        this.avatarImg = avatarImg;
    }

    // 일 대 다 매핑
    public void setMappingUser(Member member) {
        this.member = member;
        member.getAvatars().add(this);
    }

    public void addItem(AvatarItem item) {
        items.add(item);
        item.setAvatar(this);
    }

    public void update(String avatarImg) {
        this.avatarImg = avatarImg;
    }

    /**
     * 새로운 의류(garment)를 착용하는 비즈니스 로직.
     * 상의 또는 하의인 경우, 기존에 입고 있던 옷을 벗고 새로 입습니다.
     * @param garment 새로 착용할 상품 엔티티
     */
    public void wearGarment(Product garment) {
        if (garment.isUpperGarment()) {
            // 기존에 입고 있던 상의를 찾아서 벗는다(제거한다).
            removeItemByCategory(true);
        } else if (garment.isLowerGarment()) {
            // 기존에 입고 있던 하의를 찾아서 벗는다(제거한다).
            removeItemByCategory(false);
        }
        // 다른 종류의 옷은 그대로 추가 (신발, 액세서리 등)

        // 새로운 옷을 입는다(추가한다).
        this.items.add(new AvatarItem(this, garment));
    }

    /**
     * 상의 또는 하의 카테고리에 해당하는 아이템을 리스트에서 제거합니다.
     * @param isUpper 상의를 제거할지 여부 (true: 상의, false: 하의)
     */
    private void removeItemByCategory(boolean isUpper) {
        this.items.removeIf(item ->
            isUpper ? item.getProduct().isUpperGarment() : item.getProduct().isLowerGarment()
        );
    }
}
