package com.tryiton.core.member.entity;

import com.tryiton.core.common.enums.Style;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_style")
    private Style preferredStyle;

    @Column(name = "height")
    private Integer height;  // cm

    @Column(name = "weight")
    private Integer weight;  // kg

    @Column(name = "shoe_size")
    private Integer shoeSize;

    @Column(name = "profile_image_url")
    private String profileImageUrl;
}
