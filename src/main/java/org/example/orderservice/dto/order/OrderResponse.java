package org.example.orderservice.dto.order;

import lombok.Data;
import org.example.orderservice.dto.payment.PaymentResponse;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private long id;
    private String status;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private Instant createdAt;
}
