package com.tryiton.core.address.dto;

import com.tryiton.core.address.entity.Address;
import lombok.Getter;

@Getter
public class AddressResponseDto {
    private final Long addressId;
    private final String zipCode;
    private final String address;
    private final String addressDetail;
    private final String receiver;
    private final String primaryNum;
    private final String alternateNum;
    private final boolean isDefaultAddr;
    private final String deliverRequest;

    public AddressResponseDto(Address address) {
        this.addressId = address.getId();
        this.zipCode = address.getZipCode();
        this.address = address.getAddress();
        this.addressDetail = address.getAddressDetail();
        this.receiver = address.getReceiver();
        this.primaryNum = address.getPrimaryNum();
        this.alternateNum = address.getAlternateNum();
        this.isDefaultAddr = address.isDefaultAddr();
        this.deliverRequest = address.getDeliverRequest();
    }
}