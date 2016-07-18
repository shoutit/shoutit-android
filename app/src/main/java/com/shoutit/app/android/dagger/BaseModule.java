package com.shoutit.app.android.dagger;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@Module
public final class BaseModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        final Gson internalGson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(BaseProfile.class, (JsonDeserializer<BaseProfile>) (json, typeOfT, context) -> {
                    final BaseProfile baseProfile = internalGson.fromJson(json, BaseProfile.class);
                    if (baseProfile.isUser()) {
                        return internalGson.fromJson(json, User.class);
                    } else {
                        return internalGson.fromJson(json, Page.class);
                    }
                })
                .create();
    }

    @Provides
    @UiScheduler
    Scheduler provideUiScheduler() {
        return MyAndroidSchedulers.mainThread();
    }

    @Provides
    @NetworkScheduler
    Scheduler provideNetworkScheduler() {
        return Schedulers.io();
    }
}