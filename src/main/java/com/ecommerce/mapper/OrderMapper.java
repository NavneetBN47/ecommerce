package com.ecommerce.mapper;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Order entity and DTO
 */
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, AddressMapper.class})
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "shippingAddress.id", target = "shippingAddressId")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    @Mapping(source = "items", target = "items")
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);
}

/**
 * MapStruct mapper for OrderItem entity and DTO
 */
@Mapper(componentModel = "spring")
interface OrderItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    OrderItemDTO toDTO(OrderItem orderItem);

    List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);
}