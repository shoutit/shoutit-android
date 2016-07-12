package com.shoutit.app.android.api.model;

import java.util.List;

public class BusinessVerificationResponse {
    private final String message;
    private final String status;
    private final String businessName;
    private final String businessEmail;
    private final String contactPerson;
    private final String contactNumber;
    private final List<String> images;

    public BusinessVerificationResponse(String message, String status, String businessName,
                                        String businessEmail, String contactPerson, String contactNumber,
                                        List<String> images) {
        this.message = message;
        this.status = status;
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
        this.images = images;
    }

    public String getStatus() {
        return status;
    }
}
