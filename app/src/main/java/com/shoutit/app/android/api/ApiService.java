package com.shoutit.app.android.api;

import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;

import rx.Observable;

public interface ApiService {

    Observable<SignResponse> login(EmailLoginRequest request);

    Observable<SignResponse> signup(EmailSignupRequest request);
}
