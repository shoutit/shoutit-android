package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class VerifyBusinessRequest {

    @NonNull
    private final String businessName;
    @NonNull
    private final String businessEmail;
    @NonNull
    private final String contactPerson;
    @NonNull
    private final String contactNumber;
    @NonNull
    private final List<String> images;


    public VerifyBusinessRequest(@NonNull String businessName, @NonNull String businessEmail,
                                 @NonNull String contactPerson, @NonNull String contactNumber,
                                 @NonNull List<String> images) {
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
        this.images = images;
    }
}
