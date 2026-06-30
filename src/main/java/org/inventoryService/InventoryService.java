package org.inventoryService;

import org.orderService.Item;
import org.orderService.Order;
import org.inventoryService.MissingIngredient;

import org.orderService.OrderItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryService {
    private final Inventory inventory;

    public InventoryService(Inventory inventory) {
        this.inventory = inventory;
    }


    public boolean makeOrder(Order order) {
        if (!isValidOrder(order)) {
            return false;
        }
        for (org.orderService.OrderItem orderItem : order.getItems()) {
            Item item = orderItem.getItem();
            int quantity = orderItem.getQuantity();
            for (Ingredient ingredient : item.getIngredients()) {
                Optional<Ingredient> inventoryIngredientOpt = inventory.getIngredient(ingredient.getName());
                if (inventoryIngredientOpt.isPresent()) {
                    Ingredient inventoryIngredient = inventoryIngredientOpt.get();
                    inventory.useIngredients(inventoryIngredient.getName(), ingredient.getCount() * quantity);
                } else {
                    throw new RuntimeException("Ingredient not found in inventory");
                }
            }
        }
        return true;

    }


    public List<MissingIngredient> getMissingIngredients(Order order){

        Map<String, Integer> totalRequired = getStringIntegerMap(order);

        List<MissingIngredient> missingIngredientsList = new java.util.ArrayList<>();

        for (Map.Entry<String, Integer> entry : totalRequired.entrySet()) {
            String ingredientName = entry.getKey();
            int requiredCount = entry.getValue();
            Optional<Ingredient> inventoryIngredientOpt = inventory.getIngredient(ingredientName);
            int availableCount = inventoryIngredientOpt.map(Ingredient::getCount).orElse(0);

            if (availableCount < requiredCount) {
                missingIngredientsList.add(new MissingIngredient(ingredientName, requiredCount, availableCount));
            }
        }

        return missingIngredientsList;
    }

    private static Map<String, Integer> getStringIntegerMap(Order order) {
        Map<String, Integer> totalRequired = new HashMap<>();

        for (OrderItem orderItem : order.getItems()) {
            Item item = orderItem.getItem();
            int quantity = orderItem.getQuantity();
            for (Ingredient ingredient : item.getIngredients()) {
                int currentRequired = ingredient.getCount() * quantity;
                totalRequired.put(ingredient.getName(),
                        totalRequired.getOrDefault(ingredient.getName(), 0) + currentRequired);
            }
        }
        return totalRequired;
    }

    public boolean isValidOrder(Order order){
        return getMissingIngredients(order).isEmpty();
    }
}
