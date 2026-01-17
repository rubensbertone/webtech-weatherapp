package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web. bind.annotation.*;
import com.example.demo.service.WeatherDetailService;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:5173", "https://weatherapp-frontend-orzv.onrender.com"})
public class WeatherDetailController {

    @Autowired
    private WeatherDetailService weatherDetailService;

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getWeatherDetails(
            @RequestParam double lat,
            @RequestParam double lon) {

        Map<String, Object> weatherData = weatherDetailService.fetchAllWeatherData(lat, lon);
        return ResponseEntity.ok(weatherData);
    }
}