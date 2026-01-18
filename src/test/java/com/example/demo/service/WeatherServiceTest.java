package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testklasse für den {@link WeatherService}.
 * Diese Suite validiert die Funktionalität der Ortssuche (Autovervollständigung),
 * indem verschiedene API-Antwortszenarien der Xweather API simuliert werden.
 */
class WeatherServiceTest {

    /**
     * Mock für das RestTemplate zur Simulation der HTTP-Kommunikation.
     */
    @Mock
    private RestTemplate restTemplate;

    /**
     * Die zu testende Instanz des WeatherService mit injizierten Mocks.
     */
    @InjectMocks
    private WeatherService weatherService;

    /**
     * Initialisiert die Mockito-Annotationen und setzt die erforderlichen
     * Konfigurationsfelder via Reflection vor jedem Testlauf.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(weatherService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(weatherService, "baseUrl", "http://api.xweather.com");
    }

    /**
     * @test Ortssuche mit validen Daten
     * @description Überprüft, ob eine korrekte JSON-Antwort erfolgreich in eine
     * Liste von Maps umgewandelt wird. Es wird validiert, dass Name, Land und
     * Koordinaten (lat/lon) korrekt extrahiert werden.
     */
    @Test
    void searchPlaces_shouldReturnResults_whenApiReturnsValidResponse() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[{\"place\":{\"name\":\"Berlin\",\"countryFull\":\"Germany\"},\"loc\":{\"lat\":52.52,\"long\":13.405}}]}";
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(jsonResponse);

        // Act
        List<Map<String, Object>> results = weatherService.searchPlaces("Berlin");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Berlin", results.get(0).get("name"));
        assertEquals("Germany", results.get(0).get("country"));
        assertEquals(52.52, results.get(0).get("lat"));
        assertEquals(13.405, results.get(0).get("lon"));
    }

    /**
     * @test Ortssuche ohne Ergebnisse
     * @description Prüft, ob der Service eine leere Liste zurückgibt, wenn die
     * API zwar erfolgreich antwortet, aber keine passenden Orte gefunden wurden.
     */
    @Test
    void searchPlaces_shouldReturnEmptyList_whenApiReturnsEmptyResponse() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[]}";
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(jsonResponse);

        // Act
        List<Map<String, Object>> results = weatherService.searchPlaces("UnknownPlace");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * @test Fehlerbehandlung bei API-Ausfall
     * @description Stellt sicher, dass der Service bei einer Exception des
     * RestTemplates (z. B. Netzwerkfehler) nicht abstürzt, sondern eine
     * leere Liste zurückgibt.
     */
    @Test
    void searchPlaces_shouldReturnEmptyList_whenApiReturnsError() {
        // Arrange
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        // Act
        List<Map<String, Object>> results = weatherService.searchPlaces("ErrorPlace");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * @test Robuste Verarbeitung bei fehlenden Feldern
     * @description Validiert, dass der Parser fehlende optionale Felder wie
     * Land oder Koordinaten im JSON-Objekt abfängt und durch Standardwerte
     * (leerer String oder null) ersetzt.
     */
    @Test
    void searchPlaces_shouldHandleMissingFieldsGracefully() {
        // Arrange
        String jsonResponse = "{\"success\":true,\"response\":[{\"place\":{\"name\":\"Paris\"}}]}";
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(jsonResponse);

        // Act
        List<Map<String, Object>> results = weatherService.searchPlaces("Paris");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Paris", results.get(0).get("name"));
        assertEquals("", results.get(0).get("country"));
        assertNull(results.get(0).get("lat"));
        assertNull(results.get(0).get("lon"));
    }

    /**
     * @test Behandlung einer leeren Antwort
     * @description Prüft, ob eine null-Antwort vom RestTemplate sicher als
     * leere Ergebnisliste verarbeitet wird.
     */
    @Test
    void searchPlaces_shouldReturnEmptyList_whenResponseIsNull() {
        // Arrange
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(null);

        // Act
        List<Map<String, Object>> results = weatherService.searchPlaces("NullResponse");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}