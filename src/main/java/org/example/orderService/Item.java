package org.example.orderService;

import org.example.inventoryService.Ingredient;

import java.util.List;

public class Item {
    private final String type;
    private final String name;
    private final int count;
    private final List<Ingredient> ingredients;

    public Item(String type, String name, int count, List<Ingredient> ingredients) {
        this.type = type;
        this.name = name;
        this.count = count;
        this.ingredients = ingredients;
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
