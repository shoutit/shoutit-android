package com.shoutit.app.android.facebook;

import android.support.annotation.NonNull;

import java.util.List;

import javax.annotation.Nonnull;

public class FacebookPages {

    @Nonnull
    private final List<FacebookPage> data;

    public FacebookPages(@NonNull List<FacebookPage> data) {
        this.data = data;
    }

    @Nonnull
    public List<FacebookPage> getData() {
        return data;
    }

    public class FacebookPage {

        private final String id;
        private final String name;

        public FacebookPage(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}


