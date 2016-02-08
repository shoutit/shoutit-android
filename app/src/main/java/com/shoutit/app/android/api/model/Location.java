package com.shoutit.app.android.api.model;

import com.google.gson.annotations.SerializedName;

public class Location {
    private final float latitude;
    private final float longitude;
    @SerializedName("countryCode")
    private final String countryCode;
    private final String postalCode;
    private final String state;
    private final String city;
    private final String address;
    private final String googleGeocodeResponse;

    public Location(float latitude, float longitude, String countryCode, String postalCode,
                    String state, String city, String address, String googleGeocodeResponse) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode = countryCode;
        this.postalCode = postalCode;
        this.state = state;
        this.city = city;
        this.address = address;
        this.googleGeocodeResponse = googleGeocodeResponse;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getCountryCode() {
        return countryCode;
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
