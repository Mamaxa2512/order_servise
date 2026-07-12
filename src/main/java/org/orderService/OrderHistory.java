package org.orderService;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class OrderHistory {
    private final List<Order> orders = new ArrayList<>();

    public void add(Order order){
        orders.add(order);
    }

    public Optional<Order> getOrderById(int orderId) {
        return orders.stream()
                .filter(order -> order.getOrderId() == orderId)
                .findFirst();
    }
}
