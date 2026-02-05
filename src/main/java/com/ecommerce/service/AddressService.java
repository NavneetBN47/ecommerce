package com.ecommerce.service;

import com.ecommerce.dto.AddressDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Address Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressDTO> getUserAddresses(Long userId) {
        log.info("Fetching addresses for user: {}", userId);
        return addressRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long addressId) {
        log.info("Fetching address by id: {}", addressId);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        return convertToDTO(address);
    }

    @Transactional
    public AddressDTO createAddress(Long userId, AddressDTO addressDTO) {
        log.info("Creating address for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = Address.builder()
            .user(user)
            .streetAddress(addressDTO.getStreetAddress())
            .city(addressDTO.getCity())
            .state(addressDTO.getState())
            .postalCode(addressDTO.getPostalCode())
            .country(addressDTO.getCountry())
            .isDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : false)
            .addressType(addressDTO.getAddressType() != null ? addressDTO.getAddressType() : Address.AddressType.SHIPPING)
            .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with id: {}", savedAddress.getId());
        return convertToDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        log.info("Updating address: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (addressDTO.getStreetAddress() != null) address.setStreetAddress(addressDTO.getStreetAddress());
        if (addressDTO.getCity() != null) address.setCity(addressDTO.getCity());
        if (addressDTO.getState() != null) address.setState(addressDTO.getState());
        if (addressDTO.getPostalCode() != null) address.setPostalCode(addressDTO.getPostalCode());
        if (addressDTO.getCountry() != null) address.setCountry(addressDTO.getCountry());
        if (addressDTO.getIsDefault() != null) address.setIsDefault(addressDTO.getIsDefault());
        if (addressDTO.getAddressType() != null) address.setAddressType(addressDTO.getAddressType());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully");
        return convertToDTO(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        log.info("Deleting address: {}", addressId);
        
        if (!addressRepository.existsById(addressId)) {
            throw new ResourceNotFoundException("Address not found with id: " + addressId);
        }
        
        addressRepository.deleteById(addressId);
        log.info("Address deleted successfully");
    }

    private AddressDTO convertToDTO(Address address) {
        return AddressDTO.builder()
            .id(address.getId())
            .userId(address.getUser().getId())
            .streetAddress(address.getStreetAddress())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .country(address.getCountry())
            .isDefault(address.getIsDefault())
            .addressType(address.getAddressType())
            .createdAt(address.getCreatedAt())
            .updatedAt(address.getUpdatedAt())
            .build();
    }
}