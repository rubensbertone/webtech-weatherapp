package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoriteLocations")
public class FavoriteLocationController {

    @Autowired
    private FavoriteLocationRepository favoriteLocationRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping
    public List<FavoriteLocation> getFavorites(Authentication authentication) {
        String username = authentication.getName();
        return favoriteLocationRepository.findByAppUserUsername(username);
    }

    @PostMapping
    public FavoriteLocation addFavorite(@RequestBody FavoriteLocation location, Authentication authentication) {
        String username = authentication.getName();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        location.setAppUser(user);
        return favoriteLocationRepository.save(location);
    }
}