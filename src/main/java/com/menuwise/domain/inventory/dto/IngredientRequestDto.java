package com.menuwise.domain.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequestDto {

    @NotBlank(message = "Ingredient name is required")
    private String name;

    @NotBlank(message = "Unit of measurement is required")
    private String unit;

    @NotNull(message = "Current stock is required")
    @PositiveOrZero(message = "Current stock cannot be negative")
    private Double currentStock;

    @NotNull(message = "Minimum stock threshold is required")
    @PositiveOrZero(message = "Minimum stock cannot be negative")
    private Double minimumStock;

    @NotNull(message = "Cost per unit is required")
    @Positive(message = "Cost per unit must be greater than zero")
    private Double costPerUnit;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
