package org.example.orderService;

import org.example.inventoryService.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private final List<Item> order  = new ArrayList<Item>();
    public List<Item> getOrder() {
        return order;
    }

    public void addItem(String name, String type, int count, List<Ingredient> ingredients) {
        order.add(new Item(type, name, count, ingredients));
    }

    public void removeItem(String name) {
        order.removeIf(item -> item.getName().equals(name));
    }
}

