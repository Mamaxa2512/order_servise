package org;

import org.cli.Application;
import org.cli.DataInitializer;

public class Main {
    public static void main(String[] args) {
        DataInitializer dataInitializer = new DataInitializer();
        Application application = new Application(
                dataInitializer.getMenu(),
                dataInitializer.getInventory(),
                dataInitializer.getInventoryService()
        );

        application.run();
    }
}
