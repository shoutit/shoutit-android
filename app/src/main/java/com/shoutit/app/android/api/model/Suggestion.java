package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class Suggestion {
    @Nonnull
    private final String term;

    public Suggestion(@Nonnull String term) {
        this.term = term;
    }

    @Nonnull
    public String getTerm() {
        return term;
    }
}
