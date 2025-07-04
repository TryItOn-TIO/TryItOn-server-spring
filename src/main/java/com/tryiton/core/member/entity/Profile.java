package com.tryiton.core.member.entity;

import com.tryiton.core.common.enums.Style;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile")
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
//    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_style", nullable = false)
    private Style preferredStyle;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "shoe_size", nullable = false)
    private Integer shoeSize;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // 전신 사진
    @Column(name = "user_base_image_url")
    private String userBaseImageUrl;

    // 회색 아바타
    @Column(name = "avatar_base_image_url", nullable = false)
    private String avatarBaseImageUrl;
    
    //== 비즈니스 로직 ==//
    public void updateProfile(Integer height, Integer weight, Integer shoeSize) {
        if (height != null) {
            this.height = height;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (shoeSize != null) {
            this.shoeSize = shoeSize;
        }
    }
}
