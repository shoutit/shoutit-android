package com.shoutit.app.android.api.model;

import java.io.Serializable;

public class UserLocation implements Serializable {
    private final double latitude;
    private final double longitude;
    private final String country;
    private final String postalCode;
    private final String state;
    private final String city;
    private final String address;

    // Not from API
    private final boolean isFromGps;

    public static UserLocation fromGps(UserLocation userLocation) {
        return new UserLocation(userLocation.latitude, userLocation.longitude, userLocation.country,
                userLocation.postalCode, userLocation.state, userLocation.city, userLocation.address, true);
    }

    public UserLocation(double latitude, double longitude, String country, String postalCode,
                        String state, String city, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.city = city;
        this.address = address;
        this.isFromGps = false;
    }

    public UserLocation(double latitude, double longitude, String country, String postalCode,
                        String state, String city, String address, boolean isFromGps) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.city = city;
        this.address = address;
        this.isFromGps = isFromGps;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public boolean isFromGps() {
        return isFromGps;
    }


}
