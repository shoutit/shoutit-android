package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class SentShoutMessage implements BaseAdapterItem {

    private final String shoutImageUrl;
    private final String time;
    private final String price;
    private final String description;
    private final String author;

    public SentShoutMessage(String shoutImageUrl, String time, String price, String description, String author) {
        this.shoutImageUrl = shoutImageUrl;
        this.time = time;
        this.price = price;
        this.description = description;
        this.author = author;
    }

    public String getShoutImageUrl() {
        return shoutImageUrl;
    }

    public String getTime() {
        return time;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public long adapterId() {
        return 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return false;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return false;
    }
}