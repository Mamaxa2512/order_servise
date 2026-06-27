package org.example.inventoryService;

import org.example.orderService.Item;
import org.example.orderService.Order;

public class InventoryService {
    private final Inventory inventory;
    private final Order order;

    public InventoryService(Inventory inventory, Order order) {
        this.inventory = inventory;
        this.order = order;
    }

    public boolean isValidOrder() {
        for (Item item : order.getOrder()) {
            for (Ingredient ingredient : item.getIngredients()) {
                if (!inventory.isItemAvailable(ingredient.getName(), ingredient.getCount())) {
                    return false;
                }
            }
        }
        return true;
    }
}
