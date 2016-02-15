package com.shoutit.app.android.utils;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import javax.annotation.Nonnull;

public class LocationUtils {

    private static final LatLngBounds BOUNDS_WORLDWIDE = new LatLngBounds(
            new LatLng(-85, -180),       // south-west
            new LatLng(85, 180)        // north-east
    );

    public static boolean isLocationDifferenceMoreThanDelta(double originLat, double originLng,
                                                            double newLat, double newLng, double deltaInMeters) {
        final float[] results = new float[] {0.0f};
        android.location.Location.distanceBetween(originLat, originLng, newLat, newLng, results);

        return results[0] > deltaInMeters;
    }

    public static PendingResult<AutocompletePredictionBuffer> getPredictionsForQuery(
            @Nonnull GoogleApiClient googleApiClient, @Nonnull String query) {
        return Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                BOUNDS_WORLDWIDE, null);
    }
}
