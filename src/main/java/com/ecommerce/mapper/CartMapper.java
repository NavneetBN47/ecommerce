package com.ecommerce.mapper;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Cart entity and DTO
 */
@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "items", target = "items")
    @Mapping(expression = "java(cart.getTotalItemCount())", target = "totalItemCount")
    CartDTO toDTO(Cart cart);

    List<CartDTO> toDTOList(List<Cart> carts);
}

/**
 * MapStruct mapper for CartItem entity and DTO
 */
@Mapper(componentModel = "spring")
interface CartItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    CartItemDTO toDTO(CartItem cartItem);

    List<CartItemDTO> toDTOList(List<CartItem> cartItems);
}