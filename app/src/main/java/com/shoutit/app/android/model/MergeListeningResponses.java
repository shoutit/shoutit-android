package com.shoutit.app.android.model;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func2;

public class MergeListeningResponses implements Func2<ListeningResponse, ListeningResponse, ListeningResponse> {
        @Override
        public ListeningResponse call(ListeningResponse previousResponses, ListeningResponse lastResponse) {
            final List<BaseProfile> profiles = previousResponses.getProfiles();

            final List<BaseProfile> updatedProfile = new ArrayList<>(profiles);
            updatedProfile.addAll(lastResponse.getProfiles());

            return new ListeningResponse(lastResponse.getCount(), lastResponse.getNext(),
                    lastResponse.getPrevious(), updatedProfile);
        }
    }