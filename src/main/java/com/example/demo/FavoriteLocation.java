package com.example.demo;

public class FavoriteLocation {
    private String locationName;
    private String country;
    private double latitude;
    private double longitude;

    FavoriteLocation() {
    }

    public FavoriteLocation(String locationName, String country, double latitude, double longitude) {
        this.locationName = locationName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
