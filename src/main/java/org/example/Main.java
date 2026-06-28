package org.example;

import org.example.cli.DataInitializer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Order Service started");
        
        // Фаза 1: Ініціалізація даних
        DataInitializer dataInitializer = new DataInitializer();
        System.out.println("Дані ініціалізовано: " + 
                dataInitializer.getMenu().getMenu().size() + " страви у меню, " +
                dataInitializer.getInventory().getItems().size() + " інгредієнтів на складі.");
    }
}