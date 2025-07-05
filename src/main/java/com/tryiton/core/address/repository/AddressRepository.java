package com.tryiton.core.address.repository;

import com.tryiton.core.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // 특정 사용자의 모든 배송지 목록을 조회
    List<Address> findByUserId(Long userId);
}