package org.example.orderservice.dto.payment;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private BigDecimal amount;
    private String method;
}
