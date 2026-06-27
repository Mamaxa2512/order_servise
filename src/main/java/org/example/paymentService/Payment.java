package org.example.paymentService;

public class Payment {
    private int amount;
    private final String id;
    private String method;

    public Payment(String id, int amount, String method) {
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public int getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
