package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.app.Activity;

import com.appunite.rx.ResponseOrError;
import com.facebook.CallbackManager;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.view.loginintro.FacebookHelper;

import rx.Observable;
import rx.functions.Func1;

public class FacebookFriendsPresenter {

    public FacebookFriendsPresenter(final FacebookHelper facebookHelper,
                                    ApiService apiService,
                                    UserPreferences userPreferences,
                                    final Activity activity,
                                    final CallbackManager callbackManager) {

        //noinspection ConstantConditions
        final boolean hasRequiredPermissionInApi = facebookHelper.hasRequiredPermissionInApi(
                userPreferences.getUser(), FacebookHelper.PERMISSION_USER_FRIENDS);

        final Observable<>

        Observable.just(hasRequiredPermissionInApi)
                .switchMap(new Func1<Boolean, Observable<ResponseOrError<Boolean>>>() {
                    @Override
                    public Observable<ResponseOrError<Boolean>> call(Boolean hasPermissions) {
                        if (hasPermissions) {
                            return Observable.just(ResponseOrError.fromData(true);
                        } else {
                            return facebookHelper.askForPermissionIfNeeded(activity, FacebookHelper.PERMISSION_USER_FRIENDS, callbackManager, false)
                                    .map()
                        }
                    }
                })
    }

}
