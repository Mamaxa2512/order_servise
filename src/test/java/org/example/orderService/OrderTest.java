package org.example.orderService;

import org.inventoryService.Ingredient;
import org.junit.jupiter.api.Test;
import org.orderService.Item;
import org.orderService.Order;


import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderTest {

    private Item item(String name, String price) {
        return new Item(
                "Напій",
                name,
                1,
                List.of(new Ingredient("Зерна", "Кавові зерна", 10)),
                new BigDecimal(price)
        );
    }


    @Test
    void getTotalPrizeReturnsZeroFroEmptyOrder(){
        Order order = new Order(1);
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
    }

    @Test
    void getTotalPrizeWithOneItem(){
        Order order = new Order(1);
        Item item = item("Espresso", "40.00");
        order.addItem(item);
        assertEquals(new BigDecimal("40.00"), order.getTotalPrice());
    }


    @Test
    void getTotalPrizeWithMultipleItems(){
        Order order = new Order(1);
        Item item1 = item("Espresso", "40.00");
        Item item2 = item("Americano", "50.00");
        order.addItem(item1);
        order.addItem(item2);
        assertEquals(new BigDecimal("90.00"), order.getTotalPrice());
    }

    @Test
    void removeItemDeletesItemFromOrder(){
        Order order = new Order(1);
        Item item1 = item("Espresso", "40.00");
        Item item2 = item("Americano", "50.00");
        order.addItem(item1);
        order.addItem(item2);
        order.removeItem("Espresso");
        
        assertEquals(new BigDecimal("50.00"), order.getTotalPrice());
        assertEquals(1, order.getItems().size());
        assertEquals("Americano", order.getItems().get(0).getItem().getName());
    }

    @Test
    void addItemWithQuantityAddsOneOrderItemAndIncreasesQuantity() {
        Order order = new Order(1);
        Item item = item("Espresso", "40.00");
        order.addItem(item, 3);
        
        assertEquals(1, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("120.00"), order.getTotalPrice());
        
        order.addItem(item, 2);
        assertEquals(1, order.getItems().size());
        assertEquals(5, order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("200.00"), order.getTotalPrice());
    }




}
