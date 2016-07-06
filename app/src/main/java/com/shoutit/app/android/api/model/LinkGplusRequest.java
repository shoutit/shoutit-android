package com.shoutit.app.android.api.model;

public class LinkGplusRequest {
    private final String account;
    private final String gplusCode;

    public LinkGplusRequest(final String account, final String gplusCode) {
        this.account = account;
        this.gplusCode = gplusCode;
    }

    public LinkGplusRequest(final String account) {
        this.account = account;
        this.gplusCode = null;
    }
}
