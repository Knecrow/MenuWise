package com.menuwise.weather;

import com.menuwise.domain.category.Category;
import com.menuwise.domain.menu.Item;
import com.menuwise.repository.ItemRepository;
import com.menuwise.weather.dto.PrepRecommendationDto;
import com.menuwise.weather.dto.WeatherForecastDto;
import com.menuwise.weather.service.WeatherAdaptivePrepService;
import com.menuwise.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherAdaptivePrepServiceTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private ItemRepository itemRepository;

    private WeatherAdaptivePrepService prepService;

    private Item soupItem;
    private Item saladItem;
    private Item burgerItem;

    @BeforeEach
    void setUp() {
        prepService = new WeatherAdaptivePrepService(weatherService, itemRepository);

        Category appetizers = Category.builder().id(1L).name("Appetizers").build();
        Category mains = Category.builder().id(2L).name("Mains").build();

        soupItem = Item.builder().id(1L).name("Wild Mushroom Soup").category(appetizers).build();
        saladItem = Item.builder().id(2L).name("Classic Caesar Salad").category(appetizers).build();
        burgerItem = Item.builder().id(3L).name("Classic Cheeseburger").category(mains).build();

        when(itemRepository.findAll()).thenReturn(List.of(soupItem, saladItem, burgerItem));
    }

    @Test
    void computeRecommendations_UnderColdRain_ShouldScaleComfortUpAndSaladDown() {
        WeatherForecastDto rainyForecast = WeatherForecastDto.builder()
                .temperatureCelsius(8.0)
                .condition("Rain")
                .humidity(85)
                .rainProbability(0.90)
                .isInclementWeather(true)
                .build();

        List<PrepRecommendationDto> recommendations = prepService.computeRecommendations(rainyForecast);

        assertThat(recommendations).hasSize(3);

        PrepRecommendationDto soupRec = recommendations.stream()
                .filter(r -> r.getItemName().toLowerCase().contains("soup")).findFirst().orElseThrow();
        PrepRecommendationDto saladRec = recommendations.stream()
                .filter(r -> r.getItemName().toLowerCase().contains("salad")).findFirst().orElseThrow();
        PrepRecommendationDto burgerRec = recommendations.stream()
                .filter(r -> r.getItemName().toLowerCase().contains("burger")).findFirst().orElseThrow();

        // Soup scaled up +35% (20 -> 27)
        assertThat(soupRec.getWeatherMultiplier()).isEqualTo(1.35);
        assertThat(soupRec.getRecommendedQuantity()).isEqualTo(27);
        assertThat(soupRec.getRationale()).contains("+35%");

        // Salad scaled down -25% (20 -> 15)
        assertThat(saladRec.getWeatherMultiplier()).isEqualTo(0.75);
        assertThat(saladRec.getRecommendedQuantity()).isEqualTo(15);
        assertThat(saladRec.getRationale()).contains("-25%");

        // Burger remains baseline 1.0 (20 -> 20)
        assertThat(burgerRec.getWeatherMultiplier()).isEqualTo(1.0);
        assertThat(burgerRec.getRecommendedQuantity()).isEqualTo(20);
    }

    @Test
    void computeRecommendations_UnderHotClearWeather_ShouldScaleSaladUpAndSoupDown() {
        WeatherForecastDto hotForecast = WeatherForecastDto.builder()
                .temperatureCelsius(30.0)
                .condition("Clear")
                .humidity(40)
                .rainProbability(0.05)
                .isInclementWeather(false)
                .build();

        List<PrepRecommendationDto> recommendations = prepService.computeRecommendations(hotForecast);

        PrepRecommendationDto soupRec = recommendations.stream()
                .filter(r -> r.getItemName().toLowerCase().contains("soup")).findFirst().orElseThrow();
        PrepRecommendationDto saladRec = recommendations.stream()
                .filter(r -> r.getItemName().toLowerCase().contains("salad")).findFirst().orElseThrow();

        // Salad scaled up +30% in hot weather (20 -> 26)
        assertThat(saladRec.getWeatherMultiplier()).isEqualTo(1.30);
        assertThat(saladRec.getRecommendedQuantity()).isEqualTo(26);

        // Soup scaled down -15% in hot weather (20 -> 17)
        assertThat(soupRec.getWeatherMultiplier()).isEqualTo(0.85);
        assertThat(soupRec.getRecommendedQuantity()).isEqualTo(17);
    }
}
