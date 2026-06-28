package org.orderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final List<OrderItem> items = new ArrayList<>();
    private final int orderId;

    public Order(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem oi : items) {
            totalPrice = totalPrice.add(oi.getTotalPrice());
        }
        return totalPrice;
    }

    // Legacy method for tests
    public void addItem(Item item) {
        addItem(item, 1);
    }

    public void addItem(Item item, int quantity) {
        for (OrderItem oi : items) {
            if (oi.getItem().getName().equals(item.getName())) {
                oi.increaseQuantity(quantity);
                return;
            }
        }
        items.add(new OrderItem(item, quantity));
    }

    public void removeItem(String name) {
        items.removeIf(oi -> oi.getItem().getName().equals(name));
    }
}
