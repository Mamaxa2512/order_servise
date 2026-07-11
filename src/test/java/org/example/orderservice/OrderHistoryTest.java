package org.example.orderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orderService.Order;
import org.orderService.OrderHistory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderHistoryTest {

    private OrderHistory orderHistory;

    @BeforeEach
    void setUp() {
        orderHistory = new OrderHistory();
    }

    @Test
    void getOrdersReturnsEmptyListInitially() {
        assertTrue(orderHistory.getOrders().isEmpty(), "Order history should be empty initially");
    }

    @Test
    void addStoresOrderInHistory() {
        Order order = new Order(1);
        orderHistory.add(order);

        assertEquals(1, orderHistory.getOrders().size(), "Order history should contain one order");
        assertEquals(order, orderHistory.getOrders().get(0), "The stored order should match the added one");
    }

    @Test
    void getOrderByIdReturnsOrderWhenExists() {
        Order order1 = new Order(1);
        Order order2 = new Order(2);
        orderHistory.add(order1);
        orderHistory.add(order2);

        Optional<Order> foundOrder = orderHistory.getOrderById(2);

        assertTrue(foundOrder.isPresent(), "Order with ID 2 should be found");
        assertEquals(order2, foundOrder.get(), "The found order should be the correct one");
    }

    @Test
    void getOrderByIdReturnsEmptyWhenNotExists() {
        Order order = new Order(1);
        orderHistory.add(order);

        Optional<Order> foundOrder = orderHistory.getOrderById(99);

        assertFalse(foundOrder.isPresent(), "Order with ID 99 should not be found");
    }
}
