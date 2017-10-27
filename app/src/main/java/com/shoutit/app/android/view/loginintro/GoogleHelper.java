package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import javax.annotation.Nonnull;

public class GoogleHelper {

    public static void loginGoogle(@Nonnull Activity activity, final int googleSignInRequestCode) {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode("935842257865-s6069gqjq4bvpi4rcbjtdtn2kggrvi06.apps.googleusercontent.com")
                .build();

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (googleApiClient.isConnected()) {
            startLoginIntent(googleApiClient, activity, googleSignInRequestCode);
        } else {
            googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    startLoginIntent(googleApiClient, activity, googleSignInRequestCode);
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            });
            googleApiClient.connect();
        }
    }

    private static void startLoginIntent(GoogleApiClient googleApiClient, Activity activity, final int googleSignInRequestCode) {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, googleSignInRequestCode);
    }

    public static void logOutGoogle(Activity activity) {
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .build();

        if (googleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(googleApiClient);
        } else {
            googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Auth.GoogleSignInApi.signOut(googleApiClient);
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            });
            googleApiClient.connect();
        }
    }

}
