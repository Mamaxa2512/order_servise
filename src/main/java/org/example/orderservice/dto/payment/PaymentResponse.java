package org.example.orderservice.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {
    private long id;
    private BigDecimal amount;
    private String method;
    private Instant createdAt;
}
