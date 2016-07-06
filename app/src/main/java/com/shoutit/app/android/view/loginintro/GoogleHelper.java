package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shoutit.app.android.constants.RequestsConstants;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Singleton
public class GoogleHelper {

    public static void loginGoogle(@Nonnull final Activity activity) {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode("935842257865-s6069gqjq4bvpi4rcbjtdtn2kggrvi06.apps.googleusercontent.com")
                .build();

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RequestsConstants.GOOGLE_SIGN_IN);
    }
}
