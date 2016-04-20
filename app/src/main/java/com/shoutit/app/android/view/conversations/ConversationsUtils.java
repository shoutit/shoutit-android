package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.api.model.ConversationProfile;

import java.util.List;

import javax.annotation.Nonnull;

public class ConversationsUtils {

    public static String getChatWithString(@Nonnull List<ConversationProfile> profiles, @Nonnull String userId) {
        String chatWith = null;
        if (profiles.size() == 2) {
            for (final ConversationProfile profile : profiles) {
                if (!profile.getId().equals(userId)) {
                    chatWith = profile.getName();
                }
            }
        } else {
            final StringBuilder nameBuilder = new StringBuilder();
            for (final ConversationProfile profile : profiles) {
                if (!profile.getId().equals(userId)) {
                    nameBuilder.append(profile.getName());
                }
            }
            chatWith = nameBuilder.toString();
        }
        return chatWith;
    }

}
