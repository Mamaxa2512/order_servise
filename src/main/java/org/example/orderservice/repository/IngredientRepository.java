package org.example.orderservice.repository;

import org.example.orderservice.domain.ingredient.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

    Optional<IngredientEntity> findByName(String name);
}
