package org.example.orderservice.dto.item;

import lombok.Data;
import org.example.orderservice.dto.ingredient.IngredientResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemResponse {
    private long id;
    private String type;
    private String name;
    private BigDecimal price;
    private List<IngredientResponse> ingredients;

}
