package org.cli;

import org.inventoryService.Ingredient;
import org.inventoryService.Inventory;
import org.orderService.Item;
import org.orderService.Menu;
import org.orderService.Order;
import org.orderService.OrderHistory;
import org.paymentService.Payment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsoleUI {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    public void printHeader() {
        System.out.println(CYAN + "=== Cafe Order Service ===" + RESET);
    }

    public void printSeparator() {
        System.out.println("----------------------------------------------");
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1. Показати меню");
        System.out.println("2. Створити замовлення");
        System.out.println("3. Перевірити склад");
        System.out.println("4. Історія замовлень");
        System.out.println("0. Вихід");
    }

    public void printMenu(Menu menu) {
        List<Item> items = menu.getMenu();

        System.out.println();
        System.out.println("Меню:");
        System.out.printf("%-4s %-16s %-12s %10s%n", "#", "Назва", "Тип", "Ціна");
        printSeparator();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.printf("%-4d %-16s %-12s %10.2f%n",
                    i + 1,
                    item.getName(),
                    item.getType(),
                    item.getPrice());
        }
    }

    public void printInventory(Inventory inventory) {
        System.out.println();
        System.out.println("Склад:");
        System.out.printf("%-14s %-18s %10s%n", "Тип", "Назва", "Кількість");
        printSeparator();

        for (Ingredient ingredient : inventory.getItems()) {
            System.out.printf("%-14s %-18s %10d%n",
                    ingredient.getType(),
                    ingredient.getName(),
                    ingredient.getCount());
        }
    }

    public void printOrderDraft(Order order) {
        System.out.println();
        System.out.println("Поточне замовлення #" + order.getOrderId() + ":");

        if (order.getItems().isEmpty()) {
            System.out.println("Замовлення порожнє.");
            return;
        }

        printOrderItems(order);
        System.out.println("Разом: " + order.getTotalPrice());
    }

    public void printReceipt(Order order, Payment payment) {
        System.out.println();
        System.out.println(GREEN + "Оплату успішно проведено." + RESET);
        System.out.println("Чек #" + payment.getId());
        System.out.println("Метод оплати: " + payment.getMethod());
        printOrderItems(order);
        printSeparator();
        System.out.println("До сплати: " + payment.getAmount());
    }

    public void printSuccess(String message) {
        System.out.println(GREEN + message + RESET);
    }

    public void printError(String message) {
        System.out.println(RED + "Помилка: " + message + RESET);
    }

    public void printInfo(String message) {
        System.out.println(message);
    }

    private void printOrderItems(Order order) {
        System.out.printf("%-18s %6s %10s %10s%n", "Позиція", "К-сть", "Ціна", "Сума");
        printSeparator();

        for (org.orderService.OrderItem line : order.getItems()) {
            System.out.printf("%-18s %6d %10.2f %10.2f%n",
                    line.getItem().getName(),
                    line.getQuantity(),
                    line.getItem().getPrice(),
                    line.getTotalPrice());
        }
    }


    public void printMissingIngredients(List<org.inventoryService.MissingIngredient> missingIngredients) {
        System.out.println();
        System.out.println("Недостатньо інгредієнтів для замовлення:");
        System.out.printf("%-18s %10s %10s %10s%n", "Назва", "Потрібно", "Доступно", "Не вистачає");
        System.out.println("------------------------------------------------------------");

        for (org.inventoryService.MissingIngredient missing : missingIngredients) {
            System.out.printf("%-18s %10d %10d %10d%n",
                    missing.getName(),
                    missing.getRequiredCount(),
                    missing.getAvailableCount(),
                    missing.getMissingCount());
        }
    }

    public void printOrderHistory(OrderHistory orderHistory){
        System.out.println();
        System.out.println("Історія замовлень:");
        printSeparator();
        
        if (orderHistory.getOrders().isEmpty()) {
            System.out.println("Історія порожня.");
            return;
        }

        System.out.printf("%-12s %-20s %10s%n", "Замовлення", "Кількість позицій", "Сума");
        printSeparator();
        
        for (Order order : orderHistory.getOrders()) {
            int itemsCount = order.getItems().stream().mapToInt(org.orderService.OrderItem::getQuantity).sum();
            System.out.printf("#%-11d %-20d %10.2f%n",
                    order.getOrderId(),
                    itemsCount,
                    order.getTotalPrice());
        }
    }
}
