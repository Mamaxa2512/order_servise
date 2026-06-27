package org.example.inventoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Inventory {
    private final List<Ingredient> items;

    public Inventory() {
        items = new ArrayList<Ingredient>();
    }

    public List<Ingredient> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Ingredient ingredient) {
        items.add(ingredient);
    }

    public void addItem(String type, String name, int count) {
        items.add(new Ingredient(type, name, count));
    }

    public Optional<Ingredient> getIngredient(String name){
        return items.stream()
                .filter(item -> item.getName().equals(name))
                .findFirst();
    }

    public boolean isItemAvailable(String name, int count) {
        for (Ingredient item : items) {
            if (item.getName().equals(name) && item.getCount() >= count) {
                return true;
            }
        }
        return false;
    }
}
