package com.shoutit.app.android.dao;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;

import rx.functions.Func2;

public class MergeShoutsResponses implements Func2<ShoutsResponse, ShoutsResponse, ShoutsResponse> {
        @Override
        public ShoutsResponse call(ShoutsResponse previousData, ShoutsResponse newData) {
            final ImmutableList<Shout> allItems = ImmutableList.<Shout>builder()
                    .addAll(previousData.getShouts())
                    .addAll(newData.getShouts())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new ShoutsResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }