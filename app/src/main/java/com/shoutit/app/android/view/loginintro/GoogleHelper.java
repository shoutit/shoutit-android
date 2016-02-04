package com.shoutit.app.android.view.loginintro;

import android.app.Activity;
import android.content.Intent;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class GoogleHelper {

    private final static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    public static void pickUserAccount(Activity context, int requestCode) {
        final String[] accountTypes = new String[]{"com.google"};
        final Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        context.startActivityForResult(intent, requestCode);
    }

    public static Observable<String> getToken(final Activity activity, final String email, final int authRequestCode) {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            final String token = GoogleAuthUtil.getToken(activity, email, SCOPE);
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(token);
                                subscriber.onCompleted();
                            }
                        } catch (UserRecoverableAuthException e) {
                            if (!subscriber.isUnsubscribed()) {
                                final Intent intent = e.getIntent();
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.startActivityForResult(intent, authRequestCode);
                                    }
                                });
                                subscriber.onCompleted();
                            }
                        } catch (GoogleAuthException | IOException e) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(e);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread());
    }
}
