package com.shoutit.app.android.utils;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ProfilesHelper {

    public static ProfilesListResponse updateLastResponseWithListenedProfiles(ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final ProfilesListResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getResults();
        final String profileToUpdateId = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ProfilesListResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

    public static class ProfileToListenWithLastResponse {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final ProfilesListResponse response;

        public ProfileToListenWithLastResponse(@Nonnull BaseProfile profile,
                                               @Nonnull ProfilesListResponse response) {
            this.profile = profile;
            this.response = response;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nonnull
        public ProfilesListResponse getResponse() {
            return response;
        }
    }
}
