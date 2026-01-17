package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WeatherService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${xweather.client-id}")
    private String clientId;

    @Value("${xweather.client-secret}")
    private String clientSecret;

    @Value("${xweather.base-url}")
    private String baseUrl;

    public List<Map<String, Object>> searchPlaces(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/places?search=%s&client_id=%s&client_secret=%s",
                baseUrl, encodedQuery, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseSearchResults(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseSearchResults(String jsonResponse) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return results;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (root.has("response") && root.get("response").isArray()) {
                for (JsonNode place : root.get("response")) {
                    Map<String, Object> placeInfo = new HashMap<>();
                    placeInfo.put("name", place.has("name") ? place.get("name").asText() : "");
                    placeInfo.put("country", place.has("country") ? place.get("country").asText() : "");
                    placeInfo.put("lat", place.has("lat") ? place.get("lat").asDouble() : null);
                    placeInfo.put("lon", place.has("lon") ? place.get("lon").asDouble() : null);
                    results.add(placeInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}