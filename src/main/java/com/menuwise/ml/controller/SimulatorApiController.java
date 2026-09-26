package com.menuwise.ml.controller;

import com.menuwise.ml.service.PricingSimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for What-If Pricing Simulator (Iteration 4 — Person B).
 */
@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorApiController {

    private final PricingSimulatorService pricingSimulatorService;

    /**
     * Simulates impact of a price change for a specific item.
     * @param itemId        menu item ID
     * @param priceChangePct price change percent (e.g. -10 for -10%, +20 for +20%)
     */
    @GetMapping("/simulate")
    public ResponseEntity<PricingSimulatorService.SimulationResult> simulate(
            @RequestParam Long itemId,
            @RequestParam(defaultValue = "0") double priceChangePct) {
        return ResponseEntity.ok(pricingSimulatorService.simulate(itemId, priceChangePct));
    }

    /**
     * Simulates impact of a price change across ALL menu items.
     */
    @GetMapping("/simulate-all")
    public ResponseEntity<List<PricingSimulatorService.SimulationResult>> simulateAll(
            @RequestParam(defaultValue = "0") double priceChangePct) {
        return ResponseEntity.ok(pricingSimulatorService.simulateAll(priceChangePct));
    }
}
