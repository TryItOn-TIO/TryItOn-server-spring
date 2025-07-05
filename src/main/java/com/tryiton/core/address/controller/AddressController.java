package com.tryiton.core.address.controller;

import com.tryiton.core.address.dto.AddressRequestDto;
import com.tryiton.core.address.dto.AddressResponseDto;
import com.tryiton.core.address.service.AddressService;
import com.tryiton.core.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getMyAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AddressResponseDto> addresses = addressService.findMyAddresses(userDetails.getUser().getId());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<Void> addAddress(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddressRequestDto dto) {
        addressService.addAddress(userDetails.getUser().getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Void> updateAddress(@PathVariable Long addressId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody AddressRequestDto dto) {
        addressService.updateAddress(addressId, dto, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        addressService.deleteAddress(addressId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
