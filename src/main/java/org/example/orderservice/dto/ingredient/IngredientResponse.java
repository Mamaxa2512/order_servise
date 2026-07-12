package org.example.orderservice.dto.ingredient;


import lombok.Data;

@Data
public class IngredientResponse {
    private long id;
    private String type;
    private String name;
    private int stockCount;
}
