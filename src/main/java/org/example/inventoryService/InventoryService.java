package org.example.inventoryService;

import org.example.orderService.Item;
import org.example.orderService.Order;

import java.util.Optional;

public class InventoryService {
    private final Inventory inventory;

    public InventoryService(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isValidOrder(Order order) {
        for (Item item : order.getOrder()) {
            for (Ingredient ingredient : item.getIngredients()) {
                if (!inventory.isItemAvailable(ingredient.getName(), ingredient.getCount())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean makeOrder(Order order){
        if(!isValidOrder(order)){
            return false;
        }
        for(Item item: order.getOrder()){
            for(Ingredient ingredient: item.getIngredients()){
                Optional<Ingredient> inventoryIngredientOpt = inventory.getIngredient(ingredient.getName());
                if(inventoryIngredientOpt.isPresent()){
                    Ingredient inventoryIngredient = inventoryIngredientOpt.get();
                    inventoryIngredient.setCount(inventoryIngredient.getCount() - ingredient.getCount());
                }
                else{
                    return false;
                }
            }
        }
        return true;


    }
}
