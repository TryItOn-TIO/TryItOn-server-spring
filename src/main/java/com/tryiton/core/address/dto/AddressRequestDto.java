package com.tryiton.core.address.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressRequestDto {
    private String zipCode;
    private String address;
    private String addressDetail;
    private String receiver;
    private String primaryNum;
    private String alternateNum;
    private boolean isDefaultAddr;
    private String deliverRequest;
}
