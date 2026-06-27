package org.example.inventoryService;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<Ingredient> items;

    public Inventory() {
        items = new ArrayList<Ingredient>();
    }

    public ArrayList<Ingredient> getItems() {
        return items;
    }

    public void addItem(Ingredient ingredient) {
        items.add(ingredient);
    }

    public void addItem(String type, String name, int count) {
        items.add(new Ingredient(type, name, count));
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
