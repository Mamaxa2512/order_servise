package org.example.orderservice.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.orderservice.domain.base.BaseEntity;
import org.example.orderservice.domain.ingredient.IngredientEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemEntity extends BaseEntity {

    public ItemEntity(String type, String name, BigDecimal price) {
        this.type = type;
        this.name = name;
        this.price = price;
    }



    @Column(nullable = false)
    private String type;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_ingredients",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<IngredientEntity> ingredients = new HashSet<>();
}
