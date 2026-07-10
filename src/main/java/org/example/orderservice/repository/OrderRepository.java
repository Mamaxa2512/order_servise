package org.example.orderservice.repository;

import org.example.orderservice.domain.order.OrderEntity;
import org.example.orderservice.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items", "items.item"})
    List<OrderEntity> findByStatus(OrderStatus status);
}
