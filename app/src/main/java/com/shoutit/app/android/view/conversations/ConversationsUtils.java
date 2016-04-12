package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.api.model.ConversationProfile;

import java.util.List;

import javax.annotation.Nonnull;

public class ConversationsUtils {

    public static String getChatWithString(@Nonnull List<ConversationProfile> profiles) {
        String chatWith;
        if (profiles.size() == 2) {
            final ConversationProfile conversationProfile = profiles.get(0);
            chatWith = conversationProfile.getUsername();
        } else {
            final StringBuilder nameBuilder = new StringBuilder();
            for (final ConversationProfile profile : profiles) {
                if (profile.getType().equals(ConversationProfile.TYPE_USER)) {
                    nameBuilder.append(profile.getFirstName());
                } else {
                    nameBuilder.append(profile.getUsername());
                }
            }
            chatWith = nameBuilder.toString();
        }
        return chatWith;
    }

}
