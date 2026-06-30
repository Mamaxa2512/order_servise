package org.example.inventoryService;

import org.inventoryService.Ingredient;
import org.inventoryService.Inventory;
import org.inventoryService.InventoryService;
import org.junit.jupiter.api.Test;
import org.orderService.Item;
import org.orderService.Order;


import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {
    private Item itemWithCoffee(int coffeeCount) {
        return new Item(
                "Напій",
                "Тестова кава",
                1,
                List.of(new Ingredient("Зерна", "Кавові зерна", coffeeCount)),
                new BigDecimal("50.00")
        );
    }


    @Test
    void makeOrderUsesIngredientsWhenInventoryHasEnoughStock() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        order.addItem(itemWithCoffee(20));

        boolean result = service.makeOrder(order);

        assertTrue(result);
        assertEquals(80, inventory.getIngredient("Кавові зерна").orElseThrow().getCount());
    }


    @Test
    void makeOrderUsesIngredientsWhenInventoryHasNotEnoughStock() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        order.addItem(itemWithCoffee(200));

        boolean result = service.makeOrder(order);

        assertFalse(result);
        assertEquals(100, inventory.getIngredient("Кавові зерна").orElseThrow().getCount());
    }

    @Test
    void isValidOrderReturnsTrueWhenEnoughIngredients() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        order.addItem(itemWithCoffee(20));

        assertTrue(service.isValidOrder(order));
    }

    @Test
    void isValidOrderReturnsFalseWhenNotEnoughIngredients() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        order.addItem(itemWithCoffee(200));

        assertFalse(service.isValidOrder(order));
    }

    @Test
    void makeOrderSumsIngredientsForMultipleItems() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        Item coffee = itemWithCoffee(30);
        order.addItem(coffee, 2);

        boolean result = service.makeOrder(order);

        assertTrue(result);
        assertEquals(40, inventory.getIngredient("Кавові зерна").orElseThrow().getCount());
    }


    @Test
    void getMissingIngredientsReturnsCorrectMissingIngredients() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        Item coffee = itemWithCoffee(30);
        order.addItem(coffee, 5); // Total required: 150

        List<org.inventoryService.MissingIngredient> missingIngredients = service.getMissingIngredients(order);

        assertEquals(1, missingIngredients.size());
        org.inventoryService.MissingIngredient missing = missingIngredients.get(0);
        assertEquals("Кавові зерна", missing.getName());
        assertEquals(150, missing.getRequiredCount());
        assertEquals(100, missing.getAvailableCount());
    }

    @Test
    void getMissingIngredientsReturnsEmptyList(){
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        Item coffee = itemWithCoffee(30);
        order.addItem(coffee, 3); // Total required: 90

        List<org.inventoryService.MissingIngredient> missingIngredients = service.getMissingIngredients(order);

        assertTrue(missingIngredients.isEmpty());
    }

}
