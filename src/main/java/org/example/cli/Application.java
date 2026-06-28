package org.example.cli;

import org.example.inventoryService.Inventory;
import org.example.inventoryService.InventoryService;
import org.example.orderService.Item;
import org.example.orderService.Menu;
import org.example.orderService.Order;
import org.example.paymentService.Payment;

import java.util.List;
import java.util.Optional;

public class Application {
    private final Menu menu;
    private final Inventory inventory;
    private final InventoryService inventoryService;
    private final InputHandler inputHandler;
    private final ConsoleUI consoleUI;
    private int nextOrderId = 1;
    private boolean running = true;

    public Application(Menu menu, Inventory inventory, InventoryService inventoryService) {
        this.menu = menu;
        this.inventory = inventory;
        this.inventoryService = inventoryService;
        this.inputHandler = new InputHandler();
        this.consoleUI = new ConsoleUI();
    }

    public void run() {
        consoleUI.printHeader();

        while (running) {
            consoleUI.printMainMenu();
            int command = inputHandler.readInt("Оберіть дію: ");
            handleCommand(command);
        }
    }

    private void handleCommand(int command) {
        switch (command) {
            case 1 -> showMenu();
            case 2 -> createOrder();
            case 3 -> showInventory();
            case 0 -> exit();
            default -> consoleUI.printError("невідома команда.");
        }
    }

    private void showMenu() {
        consoleUI.printMenu(menu);
        inputHandler.waitForEnter();
    }

    private void showInventory() {
        consoleUI.printInventory(inventory);
        inputHandler.waitForEnter();
    }

    private void createOrder() {
        Order order = new Order(nextOrderId++);
        boolean editing = true;

        while (editing) {
            consoleUI.printMenu(menu);
            consoleUI.printOrderDraft(order);
            System.out.println();
            System.out.println("Введіть номер позиції для додавання або 0 для завершення.");

            int choice = inputHandler.readInt("Позиція: ");
            if (choice == 0) {
                editing = false;
                continue;
            }

            Optional<Item> selectedItem = findMenuItemByNumber(choice);
            if (selectedItem.isEmpty()) {
                consoleUI.printError("позицію з таким номером не знайдено.");
                continue;
            }

            int quantity = inputHandler.readInt("Кількість: ");
            if (quantity <= 0) {
                consoleUI.printError("кількість має бути більшою за нуль.");
                continue;
            }

            for (int i = 0; i < quantity; i++) {
                order.addItem(selectedItem.get());
            }

            consoleUI.printSuccess("Позицію додано до замовлення.");
        }

        completeOrder(order);
    }

    private Optional<Item> findMenuItemByNumber(int number) {
        List<Item> items = menu.getMenu();

        if (number < 1 || number > items.size()) {
            return Optional.empty();
        }

        return Optional.of(items.get(number - 1));
    }

    private void completeOrder(Order order) {
        if (order.getOrder().isEmpty()) {
            consoleUI.printInfo("Замовлення скасовано: не додано жодної позиції.");
            inputHandler.waitForEnter();
            return;
        }

        consoleUI.printOrderDraft(order);
        String confirmation = inputHandler.readString("Оплатити замовлення? (y/n): ");

        if (!confirmation.equalsIgnoreCase("y")) {
            consoleUI.printInfo("Замовлення скасовано.");
            inputHandler.waitForEnter();
            return;
        }

        try {
            boolean orderPrepared = inventoryService.makeOrder(order);
            if (!orderPrepared) {
                consoleUI.printError("недостатньо інгредієнтів на складі.");
                inputHandler.waitForEnter();
                return;
            }

            Payment payment = new Payment(
                    "PAY-" + order.getOrderId(),
                    order.getTotalPrice(),
                    "CASH"
            );
            consoleUI.printReceipt(order, payment);
        } catch (RuntimeException e) {
            consoleUI.printError(e.getMessage());
        }

        inputHandler.waitForEnter();
    }

    private void exit() {
        running = false;
        consoleUI.printInfo("До побачення!");
    }
}
