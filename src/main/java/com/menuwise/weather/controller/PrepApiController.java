package com.menuwise.weather.controller;

import com.menuwise.weather.dto.PrepRecommendationDto;
import com.menuwise.weather.dto.WeatherForecastDto;
import com.menuwise.weather.service.WeatherAdaptivePrepService;
import com.menuwise.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing weather signals and adaptive prep recommendations.
 */
@RestController
@RequestMapping("/api/v1/prep")
@RequiredArgsConstructor
public class PrepApiController {

    private final WeatherAdaptivePrepService prepService;
    private final WeatherService weatherService;

    /**
     * Endpoint providing daily kitchen prep recommendations adjusted for weather signals.
     *
     * @param city optional city filter
     * @return list of prep recommendation DTOs
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<PrepRecommendationDto>> getPrepRecommendations(
            @RequestParam(required = false) String city) {
        if (city != null && !city.trim().isEmpty()) {
            return ResponseEntity.ok(prepService.getDailyPrepRecommendationsForCity(city.trim()));
        }
        return ResponseEntity.ok(prepService.getDailyPrepRecommendations());
    }

    /**
     * Endpoint providing live weather forecast metrics for frontend dashboards.
     *
     * @param city optional target city
     * @return current weather forecast DTO
     */
    @GetMapping("/weather")
    public ResponseEntity<WeatherForecastDto> getWeatherForecast(
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(weatherService.getCurrentForecast(city));
    }
}
