package org.example.orderservice.dto.order;


import lombok.Data;
import org.example.orderservice.dto.item.ItemResponse;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
}
