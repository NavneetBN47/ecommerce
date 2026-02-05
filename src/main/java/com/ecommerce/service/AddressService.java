package com.ecommerce.service;

import com.ecommerce.dto.AddressDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Address operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    /**
     * Create address for user
     */
    public AddressDTO createAddress(Long userId, AddressDTO addressDTO) {
        log.info("Creating address for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Address address = addressMapper.toEntity(addressDTO);
        address.setUser(user);

        // If this is set as default, unset other default addresses
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.findDefaultByUserId(userId).ifPresent(defaultAddress -> {
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            });
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully for user ID: {}", userId);

        return addressMapper.toDTO(savedAddress);
    }

    /**
     * Get address by ID
     */
    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long id) {
        log.debug("Fetching address by ID: {}", id);
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + id));
        return addressMapper.toDTO(address);
    }

    /**
     * Get all addresses for user
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        log.debug("Fetching addresses for user ID: {}", userId);
        return addressRepository.findByUserId(userId).stream()
            .map(addressMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get default address for user
     */
    @Transactional(readOnly = true)
    public AddressDTO getDefaultAddress(Long userId) {
        log.debug("Fetching default address for user ID: {}", userId);
        Address address = addressRepository.findDefaultByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Default address not found for user ID: " + userId));
        return addressMapper.toDTO(address);
    }

    /**
     * Update address
     */
    public AddressDTO updateAddress(Long id, AddressDTO addressDTO) {
        log.info("Updating address with ID: {}", id);

        Address existingAddress = addressRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + id));

        // Update fields
        if (addressDTO.getAddressLine1() != null) {
            existingAddress.setAddressLine1(addressDTO.getAddressLine1());
        }
        if (addressDTO.getAddressLine2() != null) {
            existingAddress.setAddressLine2(addressDTO.getAddressLine2());
        }
        if (addressDTO.getCity() != null) {
            existingAddress.setCity(addressDTO.getCity());
        }
        if (addressDTO.getState() != null) {
            existingAddress.setState(addressDTO.getState());
        }
        if (addressDTO.getPostalCode() != null) {
            existingAddress.setPostalCode(addressDTO.getPostalCode());
        }
        if (addressDTO.getCountry() != null) {
            existingAddress.setCountry(addressDTO.getCountry());
        }
        if (addressDTO.getType() != null) {
            existingAddress.setType(addressDTO.getType());
        }

        // Handle default address change
        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !existingAddress.getIsDefault()) {
            addressRepository.findDefaultByUserId(existingAddress.getUser().getId()).ifPresent(defaultAddress -> {
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            });
            existingAddress.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(existingAddress);
        log.info("Address updated successfully with ID: {}", id);

        return addressMapper.toDTO(updatedAddress);
    }

    /**
     * Delete address
     */
    public void deleteAddress(Long id) {
        log.info("Deleting address with ID: {}", id);

        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found with ID: " + id);
        }

        addressRepository.deleteById(id);
        log.info("Address deleted successfully with ID: {}", id);
    }
}