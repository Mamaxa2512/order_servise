package org.example.orderservice.dto.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private long itemId;
    private int quantity;
}
