package com.example.demo.service;

import org.springframework.beans. factory.annotation.Autowired;
import org.springframework.beans. factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web. client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.net.URLEncoder;
import java. nio.charset.StandardCharsets;

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
        String formattedQuery = "name:^" + query;
        String encodedQuery = URLEncoder.encode(formattedQuery, StandardCharsets.UTF_8);
        String url = String. format("%s/places/search?query=%s&limit=5&client_id=%s&client_secret=%s",
                baseUrl, encodedQuery, clientId, clientSecret);

        try {
            String response = restTemplate. getForObject(url, String. class);
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
            JsonNode root = mapper. readTree(jsonResponse);

            if (root.has("response") && root.get("response").isArray()) {
                for (JsonNode item : root.get("response")) {
                    Map<String, Object> placeInfo = new HashMap<>();

                    // place Objekt enthält name, country, etc.
                    if (item. has("place")) {
                        JsonNode place = item.get("place");
                        placeInfo.put("name", place.has("name") ? place.get("name").asText() : "");
                        placeInfo.put("country", place.has("countryFull") ? place.get("countryFull").asText() : "");
                        placeInfo.put("state", place.has("state") ? place.get("state").asText() : "");
                    }

                    // loc Objekt enthält lat, long
                    if (item.has("loc")) {
                        JsonNode loc = item.get("loc");
                        placeInfo.put("lat", loc.has("lat") ? loc.get("lat").asDouble() : null);
                        placeInfo.put("lon", loc.has("long") ? loc.get("long").asDouble() : null);
                    }

                    results.add(placeInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}