package org.example.orderservice.repository;

import org.example.orderservice.domain.order.OrderEntity;
import org.example.orderservice.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items", "items.item"})
    List<OrderEntity> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.item"})
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);
}
