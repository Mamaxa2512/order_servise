package org.paymentService;

import org.orderService.Order;

import java.math.BigDecimal;
import java.util.Random;

public class PaymentService {
    int i = 1;
    public Payment pay(Order order, String method){
        BigDecimal sum = order.getTotalPrice();
        Random random = new Random();
        int paymentId = (random.nextInt(10000000, 99999999));
        return new Payment("Pay" +(i++)+ "-" +String.valueOf(paymentId), sum, method);
    }

}
