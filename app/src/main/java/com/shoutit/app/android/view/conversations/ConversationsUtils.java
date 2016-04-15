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
                    chatWith = profile.getUsername();
                }
            }
        } else {
            final StringBuilder nameBuilder = new StringBuilder();
            for (final ConversationProfile profile : profiles) {
                if (!profile.getId().equals(userId)) {
                    if (profile.getType().equals(ConversationProfile.TYPE_USER)) {
                        nameBuilder.append(profile.getFirstName());
                    } else {
                        nameBuilder.append(profile.getUsername());
                    }
                }
            }
            chatWith = nameBuilder.toString();
        }
        return chatWith;
    }

}
