package com.shoutit.app.android.api.model;

public class Location {
    private final double latitude;
    private final double longitude;
    private final String country;
    private final String postalCode;
    private final String state;
    private final String city;
    private final String address;
    private final String googleGeocodeResponse;

    public Location(double latitude, double longitude, String country, String postalCode,
                    String state, String city, String address, String googleGeocodeResponse) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.city = city;
        this.address = address;
        this.googleGeocodeResponse = googleGeocodeResponse;
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

    public String getGoogleGeocodeResponse() {
        return googleGeocodeResponse;
    }
}
