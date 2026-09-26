package com.menuwise.ml.service;

import com.menuwise.domain.inventory.Ingredient;
import com.menuwise.ml.dto.SpoilageForecastDto;
import com.menuwise.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ML-based spoilage prediction service (Iteration 3 — Person B).
 * Uses heuristic velocity model: days until expiry × current-stock vs minimum-stock ratio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WastePredictionService {

    private final IngredientRepository ingredientRepository;

    /**
     * Generates spoilage forecast for all ingredients with expiry dates.
     * Risk tiers: LOW (&lt;20%), MEDIUM (20–60%), HIGH (&gt;60%).
     */
    public List<SpoilageForecastDto> forecastSpoilage() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(7);

        List<Ingredient> ingredients = ingredientRepository.findAll();
        List<SpoilageForecastDto> forecasts = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            double riskPct = computeRiskPercentage(ingredient, today);
            String riskTier = classifyRisk(riskPct);

            double predictedWaste = computePredictedWaste(ingredient, riskPct);

            forecasts.add(SpoilageForecastDto.builder()
                    .ingredientId(ingredient.getId())
                    .ingredientName(ingredient.getName())
                    .currentStock(ingredient.getCurrentStock())
                    .predictedWasteQty(predictedWaste)
                    .spoilageRiskPercentage(riskPct)
                    .riskTier(riskTier)
                    .build());
        }

        // Sort: HIGH risk first
        forecasts.sort(Comparator.comparingDouble(SpoilageForecastDto::getSpoilageRiskPercentage).reversed());
        log.info("Spoilage forecast computed for {} ingredients", forecasts.size());
        return forecasts;
    }

    /**
     * Returns only ingredients at MEDIUM or HIGH risk.
     */
    public List<SpoilageForecastDto> getAtRiskIngredients() {
        return forecastSpoilage().stream()
                .filter(f -> !"LOW".equals(f.getRiskTier()))
                .toList();
    }

    private double computeRiskPercentage(Ingredient ingredient, LocalDate today) {
        double risk = 0.0;

        // Factor 1: Expiry proximity (max 70 points)
        if (ingredient.getExpiryDate() != null) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, ingredient.getExpiryDate());
            if (daysUntilExpiry <= 0) {
                risk += 70.0; // Already expired
            } else if (daysUntilExpiry <= 2) {
                risk += 65.0;
            } else if (daysUntilExpiry <= 5) {
                risk += 40.0;
            } else if (daysUntilExpiry <= 14) {
                risk += 20.0;
            }
        }

        // Factor 2: Stock vs minimum (excess stock = higher waste risk, max 30 points)
        if (ingredient.getMinimumStock() != null && ingredient.getMinimumStock() > 0) {
            double excessRatio = (ingredient.getCurrentStock() - ingredient.getMinimumStock())
                    / ingredient.getMinimumStock();
            if (excessRatio > 2.0) risk += 30.0;
            else if (excessRatio > 1.0) risk += 18.0;
            else if (excessRatio > 0.5) risk += 8.0;
        }

        return Math.min(100.0, risk);
    }

    private String classifyRisk(double riskPct) {
        if (riskPct >= 60.0) return "HIGH";
        if (riskPct >= 20.0) return "MEDIUM";
        return "LOW";
    }

    private double computePredictedWaste(Ingredient ingredient, double riskPct) {
        // Estimate waste as a fraction of current stock proportional to risk
        return Math.round(ingredient.getCurrentStock() * (riskPct / 100.0) * 100.0) / 100.0;
    }
}
