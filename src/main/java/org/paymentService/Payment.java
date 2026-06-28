package org.paymentService;

import java.math.BigDecimal;

public class Payment {
    private BigDecimal amount;
    private final String id;
    private String method;

    public Payment(String id, BigDecimal amount, String method) {
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
