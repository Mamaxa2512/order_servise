package org.example.orderservice.mapper;


import org.example.orderservice.domain.order.OrderEntity;
import org.example.orderservice.domain.order.OrderItemEntity;
import org.example.orderservice.dto.order.OrderItemResponse;
import org.example.orderservice.dto.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PaymentMapper.class})
public interface OrderMapper {

    OrderResponse toOrderResponse(OrderEntity orderEntity);

    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.price", target = "itemPrice")
    OrderItemResponse toOrderItemResponse(OrderItemEntity orderItemEntity);
}
