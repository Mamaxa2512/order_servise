package org.cli;

import org.inventoryService.Inventory;
import org.inventoryService.InventoryService;
import org.inventoryService.MissingIngredient;
import org.orderService.Item;
import org.orderService.Menu;
import org.orderService.Order;
import org.orderService.OrderHistory;
import org.paymentService.Payment;
import org.paymentService.PaymentService;

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
    private final PaymentService paymentService;
    private final OrderHistory orderHistory = new OrderHistory();

    public Application(Menu menu, Inventory inventory, InventoryService inventoryService, PaymentService paymentService) {
        this.menu = menu;
        this.inventory = inventory;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
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
            case 4 -> showOrderHistory();
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

    private void showOrderHistory(){
        consoleUI.printOrderHistory(orderHistory);
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

            order.addItem(selectedItem.get(), quantity);

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
        if (order.getItems().isEmpty()) {
            consoleUI.printInfo("Замовлення скасовано: не додано жодної позиції.");
            inputHandler.waitForEnter();
            return;
        }

        consoleUI.printOrderDraft(order);

        if (!inputHandler.readConfirmation("Оплатити замовлення? (y/n): ")) {
            consoleUI.printInfo("Замовлення скасовано.");
            inputHandler.waitForEnter();
            return;
        }

        try {
            List<MissingIngredient> missingIngredients = inventoryService.getMissingIngredients(order);
            if(!missingIngredients.isEmpty()){
                consoleUI.printMissingIngredients(missingIngredients);
                inputHandler.waitForEnter();
                return;
            }
            boolean orderPrepared = inventoryService.makeOrder(order);
            if (!orderPrepared) {
                consoleUI.printError("недостатньо інгредієнтів на складі.");
                inputHandler.waitForEnter();
                return;
            }

            Payment payment = paymentService.pay(order, "CASH");
            consoleUI.printReceipt(order, payment);
            orderHistory.add(order);
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
