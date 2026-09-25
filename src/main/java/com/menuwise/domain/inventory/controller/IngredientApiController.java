package com.menuwise.domain.inventory.controller;

import com.menuwise.common.exception.ResourceNotFoundException;
import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.domain.inventory.dto.IngredientRequestDto;
import com.menuwise.repository.IngredientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientApiController {

    private final IngredientRepository ingredientRepository;

    @GetMapping
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        return ResponseEntity.ok(ingredientRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
        return ResponseEntity.ok(ingredient);
    }

    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(@Valid @RequestBody IngredientRequestDto request) {
        Ingredient ingredient = Ingredient.builder()
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .costPerUnit(request.getCostPerUnit())
                .expiryDate(request.getExpiryDate())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientRepository.save(ingredient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequestDto request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));

        ingredient.setName(request.getName().trim());
        ingredient.setUnit(request.getUnit().trim());
        ingredient.setCurrentStock(request.getCurrentStock());
        ingredient.setMinimumStock(request.getMinimumStock());
        ingredient.setCostPerUnit(request.getCostPerUnit());
        ingredient.setExpiryDate(request.getExpiryDate());

        return ResponseEntity.ok(ingredientRepository.save(ingredient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingredient not found with id: " + id);
        }
        ingredientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Ingredient>> getInventoryAlerts() {
        LocalDate alertHorizon = LocalDate.now().plusDays(2);
        List<Ingredient> alerts = ingredientRepository.findLowStockOrExpiringSoon(alertHorizon);
        return ResponseEntity.ok(alerts);
    }
}
