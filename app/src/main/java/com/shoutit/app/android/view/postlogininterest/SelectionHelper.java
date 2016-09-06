package com.shoutit.app.android.view.postlogininterest;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class SelectionHelper<T> {

    private abstract class ItemAction {

        private final boolean clear;

        private final boolean selected;

        private final T localId;

        public ItemAction(boolean selected, T localId, boolean clear) {
            this.selected = selected;
            this.localId = localId;
            this.clear = clear;
        }
    }

    private class SelectAction extends ItemAction {
        public SelectAction(T id) {
            super(true, id, false);
        }
    }

    private class DeselectAction extends ItemAction {
        public DeselectAction(T id) {
            super(false, id, false);
        }
    }

    private class ClearAction extends ItemAction {
        public ClearAction() {
            super(false, null, true);
        }
    }

    private final PublishSubject<Object> itemCleared = PublishSubject.create();
    private final PublishSubject<BothParams<T, Boolean>> toggleSubject = PublishSubject.create();
    private final Observable<Set<T>> mSelectedItems;

    @Inject
    public SelectionHelper() {
        final Observable<ItemAction> selected = toggleSubject.map(new Func1<BothParams<T, Boolean>, ItemAction>() {
            @Override
            public ItemAction call(BothParams<T, Boolean> params) {
                final T id = params.param1();
                return params.param2() ? new SelectAction(id) : new DeselectAction(id);
            }
        });

        final Observable<ItemAction> itemClearMapped = itemCleared
                .map(new Func1<Object, ItemAction>() {
                    @Override
                    public ItemAction call(Object aLong) {
                        return new ClearAction();
                    }
                });

        mSelectedItems = Observable.merge(selected, itemClearMapped)
                .scan(ImmutableSet.<T>of(), new Func2<Set<T>, ItemAction, Set<T>>() {
                    @Override
                    public Set<T> call(Set<T> longs, final ItemAction itemSelected) {
                        if (itemSelected.clear) {
                            return ImmutableSet.of();
                        } else if (itemSelected.selected) {
                            return ImmutableSet.<T>builder()
                                    .addAll(longs)
                                    .add(itemSelected.localId)
                                    .build();
                        } else {
                            return ImmutableSet.copyOf(Iterables.filter(longs, new Predicate<T>() {
                                @Override
                                public boolean apply(@Nullable T input) {
                                    Preconditions.checkNotNull(input);
                                    return !input.equals(itemSelected.localId);
                                }
                            }));
                        }
                    }
                })
                .startWith(ImmutableSet.<T>of())
                .compose(ObservableExtensions.<Set<T>>behaviorRefCount());
    }

    @Nonnull
    public Observable<Set<T>> getSelectedItems() {
        return mSelectedItems;
    }

    @Nonnull
    public Observable<Boolean> itemSelectionObservable(final T item) {
        return mSelectedItems.map(new Func1<Set<T>, Boolean>() {
            @Override
            public Boolean call(Set<T> ts) {
                return ts.contains(item);
            }
        });
    }

    @Nonnull
    public Observer<BothParams<T, Boolean>> getToggleObserver() {
        return toggleSubject;
    }

    @Nonnull
    public Observer<Object> getItemClearObserver() {
        return itemCleared;
    }
}
