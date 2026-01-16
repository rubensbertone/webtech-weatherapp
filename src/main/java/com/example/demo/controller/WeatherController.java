package com.example.demo. controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org. springframework.web.bind.annotation.*;
import com.example.demo.service.WeatherService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:5173", "https://weatherapp-frontend-orzv.onrender.com"})
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/places/search")
    public ResponseEntity<List<Map<String, Object>>> searchPlaces(@RequestParam String query) {
        if (query. length() < 3) {
            return ResponseEntity.badRequest().build();
        }
        List<Map<String, Object>> results = weatherService.searchPlaces(query);
        return ResponseEntity. ok(results);
    }
}