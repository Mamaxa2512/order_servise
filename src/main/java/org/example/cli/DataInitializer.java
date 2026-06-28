package org.example.cli;

import org.example.inventoryService.Ingredient;
import org.example.inventoryService.Inventory;
import org.example.inventoryService.InventoryService;
import org.example.orderService.Item;
import org.example.orderService.Menu;

import java.math.BigDecimal;
import java.util.List;

public class DataInitializer {
    private final Inventory inventory;
    private final Menu menu;
    private final InventoryService inventoryService;

    public DataInitializer() {
        this.inventory = new Inventory();
        this.menu = new Menu();
        this.inventoryService = new InventoryService(inventory);
        initData();
    }

    private void initData() {
        // Наповнення складу базовими інгредієнтами
        inventory.addItem(new Ingredient("Рідина", "Вода", 5000));
        inventory.addItem(new Ingredient("Рідина", "Молоко", 2000));
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 1000));
        inventory.addItem(new Ingredient("Добавка", "Цукор", 500));

        // Рецепт Еспресо
        List<Ingredient> espressoIngredients = List.of(
                new Ingredient("Зерна", "Кавові зерна", 15),
                new Ingredient("Рідина", "Вода", 30)
        );
        Item espresso = new Item("Напій", "Еспресо", 1, espressoIngredients, new BigDecimal("35.00"));

        // Рецепт Американо
        List<Ingredient> americanoIngredients = List.of(
                new Ingredient("Зерна", "Кавові зерна", 15),
                new Ingredient("Рідина", "Вода", 150)
        );
        Item americano = new Item("Напій", "Американо", 1, americanoIngredients, new BigDecimal("40.00"));

        // Рецепт Капучіно
        List<Ingredient> cappuccinoIngredients = List.of(
                new Ingredient("Зерна", "Кавові зерна", 15),
                new Ingredient("Рідина", "Вода", 30),
                new Ingredient("Рідина", "Молоко", 100)
        );
        Item cappuccino = new Item("Напій", "Капучіно", 1, cappuccinoIngredients, new BigDecimal("55.00"));

        // Додаємо страви до меню
        menu.addItemToMenu(espresso);
        menu.addItemToMenu(americano);
        menu.addItemToMenu(cappuccino);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Menu getMenu() {
        return menu;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }
}
