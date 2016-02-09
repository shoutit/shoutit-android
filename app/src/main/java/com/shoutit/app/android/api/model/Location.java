package com.shoutit.app.android.api.model;

public class Location {
    private final double latitude;
    private final double longitude;
    private final String country;
    private final String postalCode;
    private final String state;
    private final String city;
    private final String address;

    public Location(double latitude, double longitude, String country, String postalCode,
                    String state, String city, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.city = city;
        this.address = address;
    }

    public static Location withCoordinates(double lat, double lng) {
        return new Location(lat, lng, null, null, null, null, null);
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

}
