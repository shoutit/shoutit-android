package com.shoutit.app.android.dao;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.PagesResponse;

import rx.functions.Func2;

public class MergePagesResponses implements Func2<PagesResponse, PagesResponse, PagesResponse> {
    @Override
    public PagesResponse call(PagesResponse previousData, PagesResponse newData) {
        final ImmutableList<Page> allItems = ImmutableList.<Page>builder()
                .addAll(previousData.getResults())
                .addAll(newData.getResults())
                .build();

        return new PagesResponse(newData.getCount(), newData.getNext(), newData.getPrevious(), allItems);
    }
}
