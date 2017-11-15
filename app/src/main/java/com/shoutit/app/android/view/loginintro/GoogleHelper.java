package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import javax.annotation.Nonnull;

public class GoogleHelper {

    public static void loginGoogle(@Nonnull Activity activity, final int googleSignInRequestCode) {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode("935842257865-s6069gqjq4bvpi4rcbjtdtn2kggrvi06.apps.googleusercontent.com")
                .build();

        final GoogleSignInClient googleClient = GoogleSignIn.getClient(activity, gso);
        final Intent signInIntent = googleClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, googleSignInRequestCode);
    }

    public static void logOutGoogle(Activity activity) {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        final GoogleSignInClient googleClient = GoogleSignIn.getClient(activity, gso);
        googleClient.signOut();
    }

}
