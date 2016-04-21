package com.shoutit.app.android.model;

import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;

import java.util.List;

import rx.functions.Func2;

public class MergeListeningResponses implements Func2<ListeningResponse, ListeningResponse, ListeningResponse> {
        @Override
        public ListeningResponse call(ListeningResponse previousResponses, ListeningResponse lastResponse) {
            final List<User> user = previousResponses.getUsers();
            final List<Page> pages = previousResponses.getPages();
            final List<TagDetail> tags = previousResponses.getTags();

            if (user != null && lastResponse.getUsers() != null) {
                user.addAll(lastResponse.getUsers());
            }

            if (pages != null && lastResponse.getPages() != null) {
                pages.addAll(lastResponse.getPages());
            }

            if (tags != null && lastResponse.getTags() != null) {
                tags.addAll(lastResponse.getTags());
            }

            return new ListeningResponse(lastResponse.getCount(), lastResponse.getNext(),
                    lastResponse.getPrevious(), user, pages, tags);
        }
    }