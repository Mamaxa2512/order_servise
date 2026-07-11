package org.example.orderservice.repository;
import org.example.orderservice.domain.item.ItemEntity;
import org.example.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.swing.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Testcontainers
public class ItemRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldGenerateIdAndCreatedAt_whenEntityIsSaved(){

        // Given
        var item = new ItemEntity("Test type", "Test name", BigDecimal.valueOf(100));


        // When
        var savedItem = itemRepository.save(item);

        // Then
        assertNotNull(savedItem.getId());
        assertNotNull(savedItem.getCreatedAt());

    }
}
