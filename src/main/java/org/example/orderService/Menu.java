package org.example.orderService;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final List<Item> menu = new ArrayList<>();

    public List<Item> getMenu() {
        return menu;
    }

    public Item getItemFromMenu(String name) {
        for (Item item : menu) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public void addItemToMenu(Item item) {
        menu.add(item);
    }

    public void removeItemFromMenu(Item item) {
        menu.remove(item);
    }
}
