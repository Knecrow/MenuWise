package com.menuwise.domain.inventory.controller;

import com.menuwise.common.exception.ResourceNotFoundException;
import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.domain.inventory.InventoryLog;
import com.menuwise.domain.inventory.dto.InventoryAdjustmentRequestDto;
import com.menuwise.repository.IngredientRepository;
import com.menuwise.repository.InventoryLogRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-logs")
@RequiredArgsConstructor
public class InventoryLogApiController {

    private final InventoryLogRepository inventoryLogRepository;
    private final IngredientRepository ingredientRepository;

    @GetMapping
    public ResponseEntity<List<InventoryLog>> getAllLogs() {
        return ResponseEntity.ok(inventoryLogRepository.findAll());
    }

    @PostMapping("/adjust")
    @Transactional
    public ResponseEntity<InventoryLog> adjustStock(@Valid @RequestBody InventoryAdjustmentRequestDto request) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + request.getIngredientId()));

        double newStock = Math.max(0.0, ingredient.getCurrentStock() + request.getChangeAmount());
        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);

        InventoryLog log = InventoryLog.builder()
                .ingredient(ingredient)
                .changeAmount(request.getChangeAmount())
                .changeType(request.getChangeType().trim())
                .timestamp(LocalDateTime.now())
                .reason(request.getReason().trim())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryLogRepository.save(log));
    }
}
