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
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@NoArgsConstructor
public class Avatar extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avater_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public Member member;

    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    public List<AvatarItem> item = new ArrayList<>();

    public void setMappingMember(Member member) {
        this.member = member;
        member.getAvatars().add(this);
    }

}
