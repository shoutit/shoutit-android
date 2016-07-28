package com.shoutit.app.android.utils;

import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListenResponse;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagsListResponse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ProfilesHelper {

    public static ProfilesListResponse updateLastResponseWithListenedProfiles(ProfileToListenWithLastResponse profileToListenWithLastResponse,
                                                                              ListenResponse data) {
        final ProfilesListResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getResults();
        final String profileToUpdateId = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile(data.getNewListenersCount());

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new ProfilesListResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

    public static TagsListResponse updateLastResponseWithListenedTag(TagToListenWithLastResponse tagToListenWithLastResponse,
                                                                     ListenResponse data) {
        final TagsListResponse response = tagToListenWithLastResponse.getResponse();

        final List<TagDetail> tags = response.getResults();
        final String tagToUpdateSlug = tagToListenWithLastResponse.getTagDetail().getSlug();

        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).getSlug().equals(tagToUpdateSlug)) {
                final TagDetail tagToUpdate = tags.get(i);
                final TagDetail updatedTag = tagToUpdate.toListenedTag(data.getNewListenersCount());

                final List<TagDetail> updatedTags = new ArrayList<>(tags);
                updatedTags.set(i, updatedTag);

                return new TagsListResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedTags);
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

    public static class TagToListenWithLastResponse {

        @Nonnull
        private final TagDetail tag;
        @Nonnull
        private final TagsListResponse response;

        public TagToListenWithLastResponse(@Nonnull TagDetail tag,
                                           @Nonnull TagsListResponse response) {
            this.tag = tag;
            this.response = response;
        }

        @Nonnull
        public TagDetail getTagDetail() {
            return tag;
        }

        @Nonnull
        public TagsListResponse getResponse() {
            return response;
        }
    }
}
