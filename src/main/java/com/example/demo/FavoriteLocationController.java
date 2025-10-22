package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class FavoriteLocationController {

    @GetMapping("/favoriteLocations")
    public List<FavoriteLocation> getFavoriteLocations() {
        return List.of(new FavoriteLocation("Oslo", "Norway", 59.9139, 10.7522)
        , new FavoriteLocation("Stockholm", "Sweden", 59.330232, 18.068381)
        , new FavoriteLocation("Copenhagen", "Denmark", 55.676111, 12.568056));
    }
}
