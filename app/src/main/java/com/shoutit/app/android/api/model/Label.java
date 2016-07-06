package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class Label {
    @NonNull
    private final String name;
    @NonNull
    private final String description;
    @NonNull
    private final String color;
    @NonNull
    private final String bgColor;

    public Label(@NonNull String name, @NonNull String description, @NonNull String color, @NonNull String bgColor) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.bgColor = bgColor;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getColor() {
        return color;
    }

    @NonNull
    public String getBgColor() {
        return bgColor;
    }
}