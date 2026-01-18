package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class WeatherDetailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${XWEATHER_CLIENT_ID}")
    private String clientId;

    @Value("${XWEATHER_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${XWEATHER_BASE_URL}")
    private String baseUrl;

    private static final Logger logger = LoggerFactory.getLogger(WeatherDetailService.class);

    public Map<String, Object> fetchAllWeatherData(double lat, double lon, String units) {
        Map<String, Object> result = new HashMap<>();

        result.put("current", fetchConditions(lat, lon, units));
        result.put("forecast", fetchForecasts(lat, lon, units));
        result.put("hourly", fetchHourlyForecasts(lat, lon, units));
        result.put("airQuality", fetchAirQuality(lat, lon));
        result.put("alerts", fetchAlerts(lat, lon));

        return result;
    }

    private Map<String, Object> fetchConditions(double lat, double lon, String units) {
        String url = String.format("%s/conditions/%s,%s?units=%s&client_id=%s&client_secret=%s",
                baseUrl, lat, lon, units, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseConditions(response, units);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Conditions für {}, {}: {}", lat, lon, e.getMessage());
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> fetchForecasts(double lat, double lon, String units) {
        String url = String.format("%s/forecasts/%s,%s?filter=day&limit=5&units=%s&client_id=%s&client_secret=%s",
                baseUrl, lat, lon, units, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseForecasts(response, units);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Forecasts für {}, {}: {}", lat, lon, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> fetchHourlyForecasts(double lat, double lon, String units) {
        String url = String.format("%s/forecasts/%s,%s?filter=1hr&limit=6&units=%s&client_id=%s&client_secret=%s",
                baseUrl, lat, lon, units, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseHourlyForecasts(response, units);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Hourly-Forecasts für {}, {}: {}", lat, lon, e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Object> fetchAirQuality(double lat, double lon) {
        String url = String.format("%s/airquality/%s,%s?client_id=%s&client_secret=%s",
                baseUrl, lat, lon, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseAirQuality(response);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der AirQuality für {}, {}: {}", lat, lon, e.getMessage());
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> fetchAlerts(double lat, double lon) {
        String url = String.format("%s/alerts/%s,%s?client_id=%s&client_secret=%s",
                baseUrl, lat, lon, clientId, clientSecret);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseAlerts(response);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Alerts für {}, {}: {}", lat, lon, e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Object> parseConditions(String jsonResponse, String units) {
        Map<String, Object> conditions = new HashMap<>();
        try {
            if (jsonResponse == null || jsonResponse.isBlank()) return conditions;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.has("success") || !root.get("success").asBoolean()) return conditions;

            if (root.has("response") && root.get("response").isArray() && root.get("response").size() > 0) {
                JsonNode responseNode = root.get("response").get(0);
                if (responseNode.has("periods") && responseNode.get("periods").isArray() && responseNode.get("periods").size() > 0) {
                    JsonNode currentData = responseNode.get("periods").get(0);

                    boolean isImperial = "e".equalsIgnoreCase(units);

                    conditions.put("temp", isImperial && currentData.has("tempF") ? currentData.get("tempF").asDouble() :
                            (currentData.has("tempC") ? currentData.get("tempC").asDouble() : null));
                    
                    conditions.put("feelsLike", isImperial && currentData.has("feelslikeF") ? currentData.get("feelslikeF").asDouble() :
                            (currentData.has("feelslikeC") ? currentData.get("feelslikeC").asDouble() : null));
                    
                    conditions.put("humidity", currentData.has("humidity") ? currentData.get("humidity").asInt() : null);
                    
                    conditions.put("windSpeed", isImperial && currentData.has("windSpeedMPH") ? currentData.get("windSpeedMPH").asDouble() :
                            (currentData.has("windSpeedKPH") ? currentData.get("windSpeedKPH").asDouble() : null));
                    
                    conditions.put("windDirection", currentData.has("windDir") ? currentData.get("windDir").asText() : null);
                    conditions.put("pressure", currentData.has("pressureMB") ? currentData.get("pressureMB").asDouble() : null);
                    
                    conditions.put("visibility", isImperial && currentData.has("visibilityMI") ? currentData.get("visibilityMI").asDouble() :
                            (currentData.has("visibilityKM") ? currentData.get("visibilityKM").asDouble() : null));
                    
                    conditions.put("uvIndex", currentData.has("uvi") ? currentData.get("uvi").asInt() : null);
                    conditions.put("cloudCover", currentData.has("sky") ? currentData.get("sky").asInt() : null);
                    conditions.put("description", currentData.has("weather") ? currentData.get("weather").asText() : null);
                    conditions.put("icon", currentData.has("icon") ? currentData.get("icon").asText() : null);
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der Conditions: {}", e.getMessage());
        }
        return conditions;
    }

    private List<Map<String, Object>> parseForecasts(String jsonResponse, String units) {
        List<Map<String, Object>> forecasts = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.isBlank()) return forecasts;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.has("success") || !root.get("success").asBoolean()) return forecasts;

            if (root.has("response") && root.get("response").isArray()) {
                JsonNode responseArray = root.get("response");
                for (JsonNode item : responseArray) {
                    if (item.has("periods") && item.get("periods").isArray()) {
                        JsonNode periods = item.get("periods");
                        boolean isImperial = "e".equalsIgnoreCase(units);

                        for (JsonNode period : periods) {
                            Map<String, Object> forecast = new HashMap<>();
                            forecast.put("timestamp", period.has("timestamp") ? period.get("timestamp").asLong() : null);
                            
                            forecast.put("tempMax", isImperial && period.has("maxTempF") ? period.get("maxTempF").asDouble() :
                                    (period.has("maxTempC") ? period.get("maxTempC").asDouble() : null));
                            
                            forecast.put("tempMin", isImperial && period.has("minTempF") ? period.get("minTempF").asDouble() :
                                    (period.has("minTempC") ? period.get("minTempC").asDouble() : null));
                            
                            forecast.put("description", period.has("weather") ? period.get("weather").asText() : null);
                            forecast.put("icon", period.has("icon") ? period.get("icon").asText() : null);
                            forecasts.add(forecast);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der Forecasts: {}", e.getMessage());
        }
        return forecasts;
    }

    private List<Map<String, Object>> parseHourlyForecasts(String jsonResponse, String units) {
        List<Map<String, Object>> hourlyForecasts = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.isBlank()) return hourlyForecasts;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.has("success") || !root.get("success").asBoolean()) return hourlyForecasts;

            if (root.has("response") && root.get("response").isArray()) {
                JsonNode responseArray = root.get("response");
                for (JsonNode item : responseArray) {
                    if (item.has("periods") && item.get("periods").isArray()) {
                        JsonNode periods = item.get("periods");
                        boolean isImperial = "e".equalsIgnoreCase(units);

                        for (JsonNode period : periods) {
                            Map<String, Object> hourly = new HashMap<>();
                            hourly.put("timestamp", period.has("timestamp") ? period.get("timestamp").asLong() : null);
                            
                            hourly.put("temp", isImperial && period.has("avgTempF") ? period.get("avgTempF").asDouble() :
                                    (period.has("avgTempC") ? period.get("avgTempC").asDouble() : null));

                            hourly.put("icon", period.has("icon") ? period.get("icon").asText() : null);
                            hourlyForecasts.add(hourly);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der Hourly-Forecasts: {}", e.getMessage());
        }
        return hourlyForecasts;
    }

    private Map<String, Object> parseAirQuality(String jsonResponse) {
        Map<String, Object> airQuality = new HashMap<>();
        try {
            if (jsonResponse == null || jsonResponse.isBlank()) return airQuality;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.has("success") || !root.get("success").asBoolean()) return airQuality;

            if (root.has("response") && root.get("response").isArray() && root.get("response").size() > 0) {
                JsonNode periods = root.get("response").get(0).get("periods");
                if (periods != null && periods.isArray() && periods.size() > 0) {
                    JsonNode latest = periods.get(0);
                    airQuality.put("aqi", latest.has("aqi") ? latest.get("aqi").asInt() : null);
                    airQuality.put("category", latest.has("category") ? latest.get("category").asText() : null);

                    if (latest.has("pollutants")) {
                        JsonNode pollutants = latest.get("pollutants");
                        if (pollutants.has("pm2p5")) {
                            airQuality.put("pm25", pollutants.get("pm2p5").get("valueMG").asDouble());
                        }
                        if (pollutants.has("pm10")) {
                            airQuality.put("pm10", pollutants.get("pm10").get("valueMG").asDouble());
                        }
                        if (pollutants.has("o3")) {
                            airQuality.put("o3", pollutants.get("o3").get("valueMG").asDouble());
                        }
                        if (pollutants.has("no2")) {
                            airQuality.put("no2", pollutants.get("no2").get("valueMG").asDouble());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der AirQuality: {}", e.getMessage());
        }
        return airQuality;
    }

    private List<Map<String, Object>> parseAlerts(String jsonResponse) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.isBlank()) return alerts;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.has("success") || !root.get("success").asBoolean()) return alerts;

            if (root.has("response") && root.get("response").isArray()) {
                JsonNode responseArray = root.get("response");
                for (JsonNode item : responseArray) {
                    if (item.has("details")) {
                        JsonNode details = item.get("details");
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("type", details.has("type") ? details.get("type").asText() : null);
                        alert.put("message", details.has("body") ? details.get("body").asText() : null);
                        alert.put("severity", details.has("priority") ? details.get("priority").asText() : null);
                        alerts.add(alert);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der Alerts: {}", e.getMessage());
        }
        return alerts;
    }
}