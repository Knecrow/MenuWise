package com.menuwise.order;

import com.menuwise.common.exception.InsufficientStockException;
import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.order.dto.OrderItemRequestDto;
import com.menuwise.order.dto.OrderRequestDto;
import com.menuwise.order.service.OrderService;
import com.menuwise.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceRollbackTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void checkoutOrder_WhenStockInsufficient_ShouldThrowExceptionAndNotDepleteInventory() {
        Ingredient patty = ingredientRepository.findById(1L).orElseThrow();
        double initialStock = patty.getCurrentStock();

        // Request an impossible quantity (e.g. 5,000 burgers)
        OrderRequestDto hugeOrder = OrderRequestDto.builder()
                .items(List.of(OrderItemRequestDto.builder()
                        .itemId(1L)
                        .quantity(5000)
                        .build()))
                .build();

        assertThatThrownBy(() -> orderService.checkoutOrder(hugeOrder))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        // Verify inventory was NOT depleted (transaction rollback succeeded)
        Ingredient reloadedPatty = ingredientRepository.findById(1L).orElseThrow();
        assertThat(reloadedPatty.getCurrentStock()).isEqualTo(initialStock);
    }
}
