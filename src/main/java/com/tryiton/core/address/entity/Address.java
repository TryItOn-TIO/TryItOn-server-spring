package com.tryiton.core.address.entity;

import com.tryiton.core.address.dto.AddressRequestDto;
import com.tryiton.core.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "addr_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
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

    @Setter // isDefaultAddr 필드만 외부에서 수정 가능하도록 Setter 추가
    @Column(nullable = false)
    private boolean isDefaultAddr = false;

    @Column(length = 255)
    private String deliverRequest;

    //== 비즈니스 로직 ==//
    public void update(AddressRequestDto dto) {
        this.zipCode = dto.getZipCode();
        this.address = dto.getAddress();
        this.addressDetail = dto.getAddressDetail();
        this.receiver = dto.getReceiver();
        this.primaryNum = dto.getPrimaryNum();
        this.alternateNum = dto.getAlternateNum();
        this.isDefaultAddr = dto.isDefaultAddr();
        this.deliverRequest = dto.getDeliverRequest();
    }
}