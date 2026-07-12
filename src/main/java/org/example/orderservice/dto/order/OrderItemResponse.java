package org.example.orderservice.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private long id;
    private String itemName;
    private BigDecimal itemPrice;
    private int quantity;
}
