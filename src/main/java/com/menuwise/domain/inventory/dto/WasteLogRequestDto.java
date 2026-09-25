package com.menuwise.domain.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteLogRequestDto {

    @NotNull(message = "Ingredient ID is required")
    private Long ingredientId;

    @NotNull(message = "Quantity wasted is required")
    @Positive(message = "Quantity wasted must be greater than zero")
    private Double quantityWasted;

    @NotNull(message = "Estimated cost is required")
    @Positive(message = "Estimated cost must be greater than zero")
    private Double estimatedCost;

    @NotBlank(message = "Reason for waste is required")
    private String reason;
}
