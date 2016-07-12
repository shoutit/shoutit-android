package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class BusinessVerificationResponse {

    @Nullable
    private final String message;
    @Nullable
    private final String status;
    @Nullable
    private final String businessName;
    @Nullable
    private final String businessEmail;
    @Nullable
    private final String contactPerson;
    @Nullable
    private final String contactNumber;
    @NonNull
    private final List<String> images;

    public BusinessVerificationResponse(@Nullable String message, @Nullable String status, @Nullable String businessName,
                                        @Nullable String businessEmail, @Nullable String contactPerson, @Nullable String contactNumber,
                                        @NonNull List<String> images) {
        this.message = message;
        this.status = status;
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
        this.images = images;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getBusinessName() {
        return businessName;
    }

    @Nullable
    public String getBusinessEmail() {
        return businessEmail;
    }

    @Nullable
    public String getContactPerson() {
        return contactPerson;
    }

    @Nullable
    public String getContactNumber() {
        return contactNumber;
    }

    @NonNull
    public List<String> getImages() {
        return images;
    }
}
