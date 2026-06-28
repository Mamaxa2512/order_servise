package org.orderService;

import java.math.BigDecimal;

public class OrderItem {
    private final Item item;
    private int quantity;

    OrderItem(Item item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return item.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to increase must be greater than zero");
        }
        quantity += amount;
    }
}
