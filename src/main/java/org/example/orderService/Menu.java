package org.example.orderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Menu {
    private final List<Item> menu = new ArrayList<>();

    public List<Item> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public Optional<Item> getItemFromMenu(String name) {
        return menu.stream()
                .filter(item -> item.getName().equals(name))
                .findFirst();
    }

    public void addItemToMenu(Item item) {
        menu.add(item);
    }

    public void removeItemFromMenu(Item item) {
        menu.remove(item);
    }
}
