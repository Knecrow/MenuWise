package com.menuwise.domain.inventory.controller;

import com.menuwise.common.exception.ResourceNotFoundException;
import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.domain.inventory.WasteLog;
import com.menuwise.domain.inventory.dto.WasteLogRequestDto;
import com.menuwise.repository.IngredientRepository;
import com.menuwise.repository.WasteLogRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/waste-logs")
@RequiredArgsConstructor
public class WasteLogApiController {

    private final WasteLogRepository wasteLogRepository;
    private final IngredientRepository ingredientRepository;

    @GetMapping
    public ResponseEntity<List<WasteLog>> getAllWasteLogs() {
        return ResponseEntity.ok(wasteLogRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WasteLog> getWasteLogById(@PathVariable Long id) {
        WasteLog log = wasteLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waste log not found with id: " + id));
        return ResponseEntity.ok(log);
    }

    @PostMapping
    public ResponseEntity<WasteLog> recordWaste(@Valid @RequestBody WasteLogRequestDto request) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + request.getIngredientId()));

        WasteLog wasteLog = WasteLog.builder()
                .ingredient(ingredient)
                .quantityWasted(request.getQuantityWasted())
                .estimatedCost(request.getEstimatedCost())
                .logDate(LocalDate.now())
                .reason(request.getReason().trim())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(wasteLogRepository.save(wasteLog));
    }
}
