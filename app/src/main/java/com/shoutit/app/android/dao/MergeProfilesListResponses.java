package com.shoutit.app.android.dao;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import rx.functions.Func2;

public class MergeProfilesListResponses implements Func2<ProfilesListResponse, ProfilesListResponse, ProfilesListResponse> {
    @Override
    public ProfilesListResponse call(ProfilesListResponse previousData, ProfilesListResponse newData) {
        final ImmutableList<BaseProfile> allItems = ImmutableList.<BaseProfile>builder()
                .addAll(previousData.getResults())
                .addAll(newData.getResults())
                .build();

        return new ProfilesListResponse(newData.getCount(), newData.getNext(), newData.getPrevious(), allItems);
    }
}