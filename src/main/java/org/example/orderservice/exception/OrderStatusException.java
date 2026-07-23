package org.example.orderservice.exception;

import org.example.orderservice.domain.order.OrderStatus;


public class OrderStatusException extends RuntimeException {
    public OrderStatusException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format("Cannot transition order from %s to %s", currentStatus, targetStatus));
    }
}
