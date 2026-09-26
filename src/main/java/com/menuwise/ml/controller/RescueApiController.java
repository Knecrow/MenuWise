package com.menuwise.ml.controller;

import com.menuwise.ml.service.RescueRecipeService;
import com.menuwise.ml.service.WastePredictionService;
import com.menuwise.ml.dto.SpoilageForecastDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Rescue Menu engine (Iteration 3 — Person B).
 */
@RestController
@RequestMapping("/api/v1/rescue")
@RequiredArgsConstructor
public class RescueApiController {

    private final RescueRecipeService rescueRecipeService;
    private final WastePredictionService wastePredictionService;

    /**
     * Returns all rescue dish proposals for at-risk ingredients.
     */
    @GetMapping("/proposals")
    public ResponseEntity<List<RescueRecipeService.RescueProposal>> getProposals() {
        return ResponseEntity.ok(rescueRecipeService.generateRescueProposals());
    }

    /**
     * Returns the full spoilage forecast for all ingredients.
     */
    @GetMapping("/spoilage-forecast")
    public ResponseEntity<List<SpoilageForecastDto>> getSpoilageForecast() {
        return ResponseEntity.ok(wastePredictionService.forecastSpoilage());
    }

    /**
     * Returns only at-risk (MEDIUM + HIGH) spoilage forecast entries.
     */
    @GetMapping("/at-risk")
    public ResponseEntity<List<SpoilageForecastDto>> getAtRiskIngredients() {
        return ResponseEntity.ok(wastePredictionService.getAtRiskIngredients());
    }
}
