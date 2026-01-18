package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteLocationRepository extends JpaRepository<FavoriteLocation, Long> {
    List<FavoriteLocation> findByAppUserUsername(String username);
    Optional<FavoriteLocation> findByAppUserUsernameAndLatitudeAndLongitude(String username, double latitude, double longitude);
}