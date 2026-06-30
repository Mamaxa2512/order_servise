package org;

import org.cli.Application;
import org.cli.DataInitializer;
import org.paymentService.PaymentService;

public class Main {
    static PaymentService paymentService = new PaymentService();
    public static void main(String[] args) {
        DataInitializer dataInitializer = new DataInitializer();
        Application application = new Application(
                dataInitializer.getMenu(),
                dataInitializer.getInventory(),
                dataInitializer.getInventoryService(),
                paymentService
        );

        application.run();
    }
}
