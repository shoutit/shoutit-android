package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class UploadContactsRequest {

    @Nonnull
    private final List<Contact> contacts;

    public UploadContactsRequest(@Nonnull List<Contact> contacts) {
        this.contacts = contacts;
    }
}
