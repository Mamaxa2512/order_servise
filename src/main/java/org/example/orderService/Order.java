package org.example.orderService;

import org.example.inventoryService.Ingredient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final List<Item> order = new ArrayList<>();
    private final int orderId;
    public Order(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
    public List<Item> getOrder() {
        return Collections.unmodifiableList(order);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Item item : order) {
            totalPrice = totalPrice.add(item.getPrice());
        }
        return totalPrice;
    }

    public void addItem(Item item) {
        order.add(item);
    }

    public void removeItem(String name) {
        order.removeIf(item -> item.getName().equals(name));
    }
}

