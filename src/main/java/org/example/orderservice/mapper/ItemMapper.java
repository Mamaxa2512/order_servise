package org.example.orderservice.mapper;

import org.example.orderservice.domain.item.ItemEntity;
import org.example.orderservice.dto.item.ItemResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = IngredientMapper.class)
public interface ItemMapper {
    ItemResponse toItemResponse(ItemEntity itemEntity);
}
