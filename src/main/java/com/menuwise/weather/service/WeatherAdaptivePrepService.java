package com.menuwise.weather.service;

import com.menuwise.domain.menu.Item;
import com.menuwise.repository.ItemRepository;
import com.menuwise.weather.dto.PrepRecommendationDto;
import com.menuwise.weather.dto.WeatherForecastDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service calculating weather-adaptive daily prep quantities based on environmental signals.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherAdaptivePrepService {

    private final WeatherService weatherService;
    private final ItemRepository itemRepository;

    /**
     * Retrieves daily prep recommendations for the default city with caching.
     */
    @Cacheable(value = "dailyPrepRecommendations")
    public List<PrepRecommendationDto> getDailyPrepRecommendations() {
        WeatherForecastDto forecast = weatherService.getDefaultForecast();
        return computeRecommendations(forecast);
    }

    /**
     * Retrieves daily prep recommendations for a specified city.
     */
    public List<PrepRecommendationDto> getDailyPrepRecommendationsForCity(String city) {
        WeatherForecastDto forecast = weatherService.getCurrentForecast(city);
        return computeRecommendations(forecast);
    }

    /**
     * Core elasticity algorithm mapping forecast conditions to inventory prep multipliers.
     */
    public List<PrepRecommendationDto> computeRecommendations(WeatherForecastDto forecast) {
        log.info("Computing weather-adaptive daily prep recommendations for condition: {}, temp: {}°C",
                forecast.getCondition(), forecast.getTemperatureCelsius());

        List<Item> items = itemRepository.findAll();
        List<PrepRecommendationDto> recommendations = new ArrayList<>();

        double temp = forecast.getTemperatureCelsius() != null ? forecast.getTemperatureCelsius() : 20.0;
        boolean isInclement = Boolean.TRUE.equals(forecast.getIsInclementWeather())
                || "Rain".equalsIgnoreCase(forecast.getCondition())
                || "Snow".equalsIgnoreCase(forecast.getCondition())
                || "Thunderstorm".equalsIgnoreCase(forecast.getCondition());

        for (Item item : items) {
            int baseline = 20; // Default baseline daily servings
            double multiplier = 1.0;
            String rationale;

            String lowerName = item.getName() != null ? item.getName().toLowerCase() : "";
            String categoryName = (item.getCategory() != null && item.getCategory().getName() != null)
                    ? item.getCategory().getName().toLowerCase() : "";

            if (isComfortHotDish(lowerName, categoryName)) {
                if (isInclement || temp < 12.0) {
                    multiplier = 1.35;
                    rationale = "Scale up +35%: Cold/rainy weather increases customer demand for hot comfort foods and soups.";
                } else if (temp > 28.0) {
                    multiplier = 0.85;
                    rationale = "Scale down -15%: Hot weather slightly dampens demand for heavy hot dishes.";
                } else {
                    rationale = "Baseline prep: Moderate weather maintains standard comfort food consumption.";
                }
            } else if (isChilledOrRefreshing(lowerName, categoryName)) {
                if (isInclement || temp < 12.0) {
                    multiplier = 0.75;
                    rationale = "Scale down -25%: Cold & rainy weather decreases demand for chilled drinks and raw salads.";
                } else if (temp > 24.0) {
                    multiplier = 1.30;
                    rationale = "Scale up +30%: Warm/sunny conditions boost demand for refreshing beverages and salads.";
                } else {
                    rationale = "Baseline prep: Normal consumption expected under mild weather.";
                }
            } else {
                multiplier = 1.0;
                rationale = "Normal baseline prep: Weather has neutral elasticity on this staple.";
            }

            int recommended = (int) Math.round(baseline * multiplier);

            recommendations.add(PrepRecommendationDto.builder()
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .baselineQuantity(baseline)
                    .recommendedQuantity(recommended)
                    .weatherMultiplier(multiplier)
                    .rationale(rationale)
                    .weatherCondition(forecast.getCondition())
                    .forecastTemperatureCelsius(temp)
                    .build());
        }

        return recommendations;
    }

    private boolean isComfortHotDish(String name, String category) {
        return name.contains("soup") || name.contains("cocoa") || name.contains("fettuccine")
                || name.contains("stew") || name.contains("curry") || name.contains("hot");
    }

    private boolean isChilledOrRefreshing(String name, String category) {
        return name.contains("salad") || name.contains("iced") || name.contains("latte")
                || name.contains("smoothie") || name.contains("juice") || name.contains("cold");
    }
}
