package com.tryiton.core.address.service;

import com.tryiton.core.address.dto.AddressRequestDto;
import com.tryiton.core.address.dto.AddressResponseDto;
import com.tryiton.core.address.entity.Address;
import com.tryiton.core.address.repository.AddressRepository;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<AddressResponseDto> findMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addAddress(Long userId, AddressRequestDto dto) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 만약 새로운 주소를 기본 배송지로 설정한다면, 기존의 다른 주소들은 모두 기본 배송지에서 해제
        if (dto.isDefaultAddr()) {
            addressRepository.findByUserId(userId).forEach(addr -> addr.setDefaultAddr(false));
        }

        Address address = Address.builder()
                .user(member)
                .zipCode(dto.getZipCode()).address(dto.getAddress()).addressDetail(dto.getAddressDetail())
                .receiver(dto.getReceiver()).primaryNum(dto.getPrimaryNum()).alternateNum(dto.getAlternateNum())
                .isDefaultAddr(dto.isDefaultAddr()).deliverRequest(dto.getDeliverRequest())
                .build();
        addressRepository.save(address);
    }

    @Transactional
    public void updateAddress(Long addressId, AddressRequestDto dto, Long userId) {
        Address address = findAddressByIdAndUserId(addressId, userId);

        if (dto.isDefaultAddr()) {
            addressRepository.findByUserId(userId).forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setDefaultAddr(false);
                }
            });
        }
        address.update(dto);
    }

    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        addressRepository.delete(address);
    }

    private Address findAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findById(addressId)
                .filter(addr -> addr.getUser().getId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("해당 주소를 찾을 수 없거나, 접근 권한이 없습니다."));
    }
}
