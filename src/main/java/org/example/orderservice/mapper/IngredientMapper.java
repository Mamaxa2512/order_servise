package org.example.orderservice.mapper;

import org.example.orderservice.domain.ingredient.IngredientEntity;
import org.example.orderservice.dto.ingredient.IngredientResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientResponse toIngredientResponse(IngredientEntity ingredientEntity);
}
