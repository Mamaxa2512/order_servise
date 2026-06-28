package org.inventoryService;

import org.orderService.Item;
import org.orderService.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryService {
    private final Inventory inventory;

    public InventoryService(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isValidOrder(Order order) {
        Map<String, Integer> totalRequired = new HashMap<>();

        for (Item item : order.getOrder()) {
            for (Ingredient ingredient : item.getIngredients()) {
                int currentRequired = ingredient.getCount() * item.getCount();
                totalRequired.put(ingredient.getName(),
                        totalRequired.getOrDefault(ingredient.getName(), 0) + currentRequired);
            }
        }

        for (Map.Entry<String, Integer> entry : totalRequired.entrySet()) {
            if (!inventory.isItemAvailable(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    public boolean makeOrder(Order order) {
        if (!isValidOrder(order)) {
            return false;
        }
        for (Item item : order.getOrder()) {
            for (Ingredient ingredient : item.getIngredients()) {
                Optional<Ingredient> inventoryIngredientOpt = inventory.getIngredient(ingredient.getName());
                if (inventoryIngredientOpt.isPresent()) {
                    Ingredient inventoryIngredient = inventoryIngredientOpt.get();
                    inventory.useIngredients(inventoryIngredient.getName(), ingredient.getCount() * item.getCount());
                } else {
                    throw new RuntimeException("Ingredient not found in inventory");
                }
            }
        }
        return true;

    }
}
