package com.shoutit.app.android.api.model;

public class UserLocationSimple {

        private final double latitude;
        private final double longitude;

        public UserLocationSimple(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }