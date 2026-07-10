package org.example.orderservice.domain.ingredient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.orderservice.domain.base.BaseEntity;

@Entity
@Table(name = "ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientEntity extends BaseEntity {

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int stockCount;

    public void deductStock(int amount) {
        if (this.stockCount < amount) {
            throw new IllegalArgumentException("Not enough stock for ingredient: " + name);
        }
        this.stockCount -= amount;
    }
}
