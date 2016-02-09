package com.shoutit.app.android.utils;


public class LocationUtils {

    public static boolean isLocationDifferenceMoreThanDelta(double originLat, double originLng,
                                                            double newLat, double newLng, double deltaInMeters) {
        final float[] results = new float[] {0.0f};
        android.location.Location.distanceBetween(originLat, originLng, newLat, newLng, results);

        return results[0] > deltaInMeters;
    }
}
