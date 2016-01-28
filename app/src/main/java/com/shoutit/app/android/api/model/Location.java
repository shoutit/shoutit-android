package com.shoutit.app.android.api.model;

public class Location {
    private final float latitude;
    private final float longitude;
    private final String country;
    private final String postalCode;
    private final String state;
    private final String city;
    private final String address;
    private final String googleGeocodeResponse;

    public Location(float latitude, float longitude, String country, String postalCode,
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
}
