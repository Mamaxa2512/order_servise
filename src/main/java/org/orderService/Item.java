package org.orderService;

import org.inventoryService.Ingredient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(type, item.type) && Objects.equals(name, item.name) && Objects.equals(ingredients, item.ingredients) && Objects.equals(price, item.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, ingredients, price);
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
