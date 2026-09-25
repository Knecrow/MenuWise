package com.menuwise.order.service;

import com.menuwise.common.exception.InsufficientStockException;
import com.menuwise.common.exception.ResourceNotFoundException;
import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.domain.inventory.InventoryLog;
import com.menuwise.domain.menu.Item;
import com.menuwise.domain.menu.ItemIngredient;
import com.menuwise.domain.order.Order;
import com.menuwise.domain.order.OrderItem;
import com.menuwise.domain.order.OrderStatus;
import com.menuwise.order.dto.OrderItemRequestDto;
import com.menuwise.order.dto.OrderRequestDto;
import com.menuwise.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final IngredientRepository ingredientRepository;
    private final ItemIngredientRepository itemIngredientRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional
    public Order checkoutOrder(OrderRequestDto request) {
        // calculate required ingredients based on recipe BOM
        Map<Long, Double> totalIngredientUsage = new HashMap<>();
        Map<Long, Item> itemCache = new HashMap<>();

        for (OrderItemRequestDto itemReq : request.getItems()) {
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + itemReq.getItemId()));
            itemCache.put(item.getId(), item);

            List<ItemIngredient> boms = itemIngredientRepository.findByItemId(item.getId());
            for (ItemIngredient bom : boms) {
                Long ingredientId = bom.getIngredient().getId();
                double needed = bom.getQuantityRequired() * itemReq.getQuantity();
                totalIngredientUsage.merge(ingredientId, needed, Double::sum);
            }
        }

        // check if enough stock is available
        Map<Long, Ingredient> ingredientCache = new HashMap<>();
        for (Map.Entry<Long, Double> entry : totalIngredientUsage.entrySet()) {
            Long ingId = entry.getKey();
            Double requiredQty = entry.getValue();

            Ingredient ingredient = ingredientRepository.findById(ingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + ingId));
            ingredientCache.put(ingId, ingredient);

            if (ingredient.getCurrentStock() < requiredQty) {
                throw new InsufficientStockException(String.format(
                        "Insufficient stock for '%s'. Required: %.2f %s, Available: %.2f %s",
                        ingredient.getName(), requiredQty, ingredient.getUnit(),
                        ingredient.getCurrentStock(), ingredient.getUnit()
                ));
            }
        }

        // create order
        Order order = Order.builder()
                .orderTimestamp(LocalDateTime.now())
                .status(OrderStatus.COMPLETED)
                .totalAmount(0.0)
                .build();
        order = orderRepository.save(order);

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemReq : request.getItems()) {
            Item item = itemCache.get(itemReq.getItemId());
            double lineTotal = item.getSellingPrice() * itemReq.getQuantity();
            totalAmount += lineTotal;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(item.getSellingPrice())
                    .build();
            orderItems.add(orderItemRepository.save(orderItem));
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        order = orderRepository.save(order);

        // deduct stock and record logs
        for (Map.Entry<Long, Double> entry : totalIngredientUsage.entrySet()) {
            Ingredient ingredient = ingredientCache.get(entry.getKey());
            Double deductedQty = entry.getValue();

            ingredient.setCurrentStock(ingredient.getCurrentStock() - deductedQty);
            ingredientRepository.save(ingredient);

            InventoryLog auditLog = InventoryLog.builder()
                    .ingredient(ingredient)
                    .changeAmount(-deductedQty)
                    .changeType("ORDER_DEDUCTION")
                    .timestamp(LocalDateTime.now())
                    .reason("Deducted for Order #" + order.getId())
                    .build();
            inventoryLogRepository.save(auditLog);
        }

        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
