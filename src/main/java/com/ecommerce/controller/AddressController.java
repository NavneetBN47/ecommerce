package com.ecommerce.controller;

import com.ecommerce.dto.AddressDTO;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Address operations
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "APIs for managing user addresses")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create address for user")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO createdAddress = addressService.createAddress(userId, addressDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Address created successfully", createdAddress));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById(@PathVariable Long id) {
        AddressDTO address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all addresses for user")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddressesByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/user/{userId}/default")
    @Operation(summary = "Get default address for user")
    public ResponseEntity<ApiResponse<AddressDTO>> getDefaultAddress(@PathVariable Long userId) {
        AddressDTO address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddress(id, addressDTO);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updatedAddress));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
}