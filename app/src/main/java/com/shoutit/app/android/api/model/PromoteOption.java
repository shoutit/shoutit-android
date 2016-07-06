package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PromoteOption {

    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
    @NonNull
    private final Label label;
    private final int credits;
    @Nullable
    private final Integer days;

    public PromoteOption(@NonNull String id, @NonNull String name,
                         @NonNull String description, @NonNull Label label, int credits,
                         @Nullable Integer days) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.label = label;
        this.credits = credits;
        this.days = days;
    }

    @NonNull
    public String getId() {
        return id;
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
    public Label getLabel() {
        return label;
    }

    public int getCredits() {
        return credits;
    }

    @Nullable
    public Integer getDays() {
        return days;
    }


}
