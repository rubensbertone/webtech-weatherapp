package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testklasse für den {@link WeatherDetailService}.
 * Diese Suite überprüft die Aggregation und das korrekte Parsing verschiedener Wetterdaten-Komponenten
 * unter Berücksichtigung des neuen Einheiten-Parameters (units).
 */
class WeatherDetailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherDetailService weatherDetailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherDetailService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(weatherDetailService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(weatherDetailService, "baseUrl", "http://api.xweather.com");
    }

    /**
     * @test Vollständigkeit der Datenstruktur
     * @description Prüft, ob alle Komponenten-Keys vorhanden sind, wenn die Methode mit 3 Parametern aufgerufen wird.
     */
    @Test
    void fetchAllWeatherData_shouldReturnAllComponents() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

        // Act - Jetzt mit 3 Argumenten (lat, lon, units)
        Map<String, Object> result = weatherDetailService.fetchAllWeatherData(52.52, 13.405, "m");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("current"));
        assertTrue(result.containsKey("forecast"));
        assertTrue(result.containsKey("hourly"));
        assertTrue(result.containsKey("airQuality"));
        assertTrue(result.containsKey("alerts"));
    }

    /**
     * @test Parsing der aktuellen Wetterbedingungen
     * @description Prüft das korrekte Mapping der Temperatur- und Feuchtigkeitswerte.
     */
    @Test
    void fetchAllWeatherData_shouldParseCurrentConditionsCorrectly() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[{\"periods\":[{\"tempC\":20.5,\"humidity\":60,\"weather\":\"Sunny\"}]}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/conditions/")) {
                return jsonResponse;
            }
            return "{}";
        });

        // Act - units="m" übergeben
        Map<String, Object> result = weatherDetailService.fetchAllWeatherData(52.52, 13.405, "m");
        Map<String, Object> current = (Map<String, Object>) result.get("current");

        // Assert
        assertNotNull(current);
        assertEquals(20.5, current.get("temp"));
        assertEquals(60, current.get("humidity"));
        assertEquals("Sunny", current.get("description"));
    }

    /**
     * @test Parsing der Wettervorhersage
     */
    @Test
    void fetchAllWeatherData_shouldParseForecastsCorrectly() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[{\"periods\":[{\"timestamp\":1620000000,\"maxTempC\":25.0,\"minTempC\":15.0}]}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/forecasts/") && url.contains("filter=day")) {
                return jsonResponse;
            }
            return "{}";
        });

        // Act
        Map<String, Object> result = weatherDetailService.fetchAllWeatherData(52.52, 13.405, "m");
        List<Map<String, Object>> forecast = (List<Map<String, Object>>) result.get("forecast");

        // Assert
        assertNotNull(forecast);
        assertEquals(1, forecast.size());
        assertEquals(25.0, forecast.get(0).get("tempMax"));
        assertEquals(15.0, forecast.get(0).get("tempMin"));
    }

    /**
     * @test Parsing der Luftqualität
     */
    @Test
    void fetchAllWeatherData_shouldParseAirQualityCorrectly() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[{\"periods\":[{\"aqi\":50,\"category\":\"Good\"}]}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/airquality/")) {
                return jsonResponse;
            }
            return "{}";
        });

        // Act
        Map<String, Object> result = weatherDetailService.fetchAllWeatherData(52.52, 13.405, "m");
        Map<String, Object> airQuality = (Map<String, Object>) result.get("airQuality");

        // Assert
        assertNotNull(airQuality);
        assertEquals(50, airQuality.get("aqi"));
        assertEquals("Good", airQuality.get("category"));
    }

    /**
     * @test Fehlerbehandlung bei API-Ausfällen
     */
    @Test
    void fetchAllWeatherData_shouldHandleApiErrorsGracefully() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        // Act
        Map<String, Object> result = weatherDetailService.fetchAllWeatherData(52.52, 13.405, "m");

        // Assert
        assertNotNull(result);
        assertTrue(((Map) result.get("current")).isEmpty());
        assertTrue(((List) result.get("forecast")).isEmpty());
        assertTrue(((List) result.get("hourly")).isEmpty());
        assertTrue(((Map) result.get("airQuality")).isEmpty());
        assertTrue(((List) result.get("alerts")).isEmpty());
    }
}