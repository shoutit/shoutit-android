package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class Currency {

    private final String code;
    private final String country;
    private final String name;

    public Currency(@NonNull String code, @NonNull String country, @NonNull String name) {
        this.code = code;
        this.country = country;
        this.name = name;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getCountry() {
        return country;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
