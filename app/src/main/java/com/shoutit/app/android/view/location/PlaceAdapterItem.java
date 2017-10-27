package com.shoutit.app.android.view.location;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

import rx.Observer;

public class PlaceAdapterItem implements BaseAdapterItem {
        @Nonnull
        private final String placeId;
        @Nonnull
        private final String fullText;
        @Nonnull
        private final Observer<String> locationSelectedObserver;

        public PlaceAdapterItem(@Nonnull String placeId,
                                 @Nonnull String fullText,
                                 @Nonnull Observer<String> locationSelectedObserver) {
            this.placeId = placeId;
            this.fullText = fullText;
            this.locationSelectedObserver = locationSelectedObserver;
        }

        @Nonnull
        public String getFullText() {
            return fullText;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        public void locationSelected() {
            locationSelectedObserver.onNext(placeId);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof PlaceAdapterItem && item.equals(this);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof PlaceAdapterItem && item.equals(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlaceAdapterItem)) return false;
            final PlaceAdapterItem that = (PlaceAdapterItem) o;
            return Objects.equal(placeId, that.placeId) &&
                    Objects.equal(fullText, that.fullText);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(placeId, fullText);
        }
    }