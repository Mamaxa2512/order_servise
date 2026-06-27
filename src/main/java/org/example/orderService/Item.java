package org.example.orderService;

import org.example.inventoryService.Ingredient;

import java.math.BigDecimal;
import java.util.List;

public class Item {
    private final String type;
    private final String name;
    private final int count;
    private final List<Ingredient> ingredients;
    private BigDecimal price;

    public Item(String type, String name, int count, List<Ingredient> ingredients, BigDecimal price) {
        this.type = type;
        this.name = name;
        this.count = count;
        this.ingredients = ingredients;
        this.price = price;
    }


    public BigDecimal getPrice(){
        return this.price;
    }

    public void setPrice(BigDecimal price){
        this.price = price;
    }
    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
