package com.tryiton.core.address.entity;

import com.tryiton.core.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(length = 255)
    private String addressDetail;

    @Column(nullable = false, length = 50)
    private String receiver;

    @Column(nullable = false, length = 20)
    private String primaryNum;

    @Column(length = 20)
    private String alternateNum;

    @Column(nullable = false)
    private boolean isDefaultAddr = false;

    @Column(length = 255)
    private String deliverRequest;
}