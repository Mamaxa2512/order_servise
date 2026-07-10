package org.example.orderservice.repository;


import org.example.orderservice.domain.item.ItemEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @EntityGraph(attributePaths = {"ingredients"})
    Optional<ItemEntity> findByName(String name);
}
