package com.shoutit.app.android.view.credits.transactions;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Transaction;
import com.shoutit.app.android.api.model.TransactionRsponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.DateTimeUtils;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TransactionsPresenter {

    final OperatorMergeNextToken<TransactionRsponse, Object> loadMoreOperator =
            OperatorMergeNextToken.create(new Func1<TransactionRsponse, Observable<TransactionRsponse>>() {

                @Override
                public Observable<TransactionRsponse> call(TransactionRsponse conversationsResponse) {
                    if (conversationsResponse == null || conversationsResponse.getPrevious() != null) {
                        if (conversationsResponse == null) {
                            return getRequest(null);
                        } else {
                            final String before = Uri.parse(conversationsResponse.getPrevious()).getQueryParameter("before");
                            return Observable.just(
                                    conversationsResponse)
                                    .zipWith(
                                            getRequest(before),
                                            (conversationsResponse1, newResponse) -> {
                                                return new TransactionRsponse(ImmutableList.copyOf(Iterables.concat(
                                                        conversationsResponse1.getResults(),
                                                        newResponse.getResults())), newResponse.getPrevious(), newResponse.getNext());
                                            });
                        }
                    } else {
                        return Observable.never();
                    }
                }
            });

    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final Context mResources;
    private CompositeSubscription mCompositeSubscription;
    private final PublishSubject<Object> requestSubject = PublishSubject.create();

    @Inject
    public TransactionsPresenter(@NonNull ApiService apiService, @NetworkScheduler Scheduler networkScheduler, @UiScheduler Scheduler uiScheduler, @Nonnull @ForActivity Context context) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mResources = context;
    }

    public void register(Listener listener) {
        final Observable<List<BaseAdapterItem>> dataObservable = requestSubject
                .startWith(new Object())
                .lift(loadMoreOperator)
                .map(TransactionRsponse::getResults)
                .map((Func1<List<Transaction>, List<BaseAdapterItem>>) transactions -> {
                    final Iterable<BaseAdapterItem> transform = Iterables.transform(transactions, new Function<Transaction, BaseAdapterItem>() {
                        @Nullable
                        @Override
                        public BaseAdapterItem apply(@Nullable Transaction input) {
                            assert input != null;
                            final Transaction.Display display = input.getDisplay();
                            final List<Transaction.Display.Ranges> ranges = display.getRanges();

                            final int offset;
                            final int end;
                            if (ranges == null || ranges.isEmpty()) {
                                offset = 0;
                                end = 0;
                            } else {
                                final Transaction.Display.Ranges range = ranges.get(0);
                                offset = range.getOffset();
                                end = range.getLength();
                            }

                            return new TransactionItem(input.getId(), createSpannedText(display.getText(), offset, end), getTime(input.getCreatedAt()), input.isOut());
                        }
                    });
                    return ImmutableList.copyOf(transform);
                });

        mCompositeSubscription = new CompositeSubscription(dataObservable
                .subscribe(listener::setData));
    }

    @NonNull
    private Observable<TransactionRsponse> getRequest(@Nullable String timestamp) {
        return mApiService.getTransactions(timestamp)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler);
    }

    private String getTime(long createdAt) {
        final long createdAtMillis = createdAt * 1000L;
        return DateTimeUtils.timeAgoFromDate(createdAtMillis);
    }

    private CharSequence createSpannedText(String text, int offset, int length) {
        final Spannable spannedString = new SpannableString(text);
        final int color = ContextCompat.getColor(mResources, R.color.accent_blue);
        spannedString.setSpan(new ForegroundColorSpan(color), offset, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannedString;
    }

    public Observer<Object> loadMoreObserver() {
        return requestSubject;
    }

    public void unregister() {
        mCompositeSubscription.unsubscribe();
    }

    public interface Listener {
        void setData(List<BaseAdapterItem> transactions);
    }

    public static class TransactionItem implements BaseAdapterItem {

        private final String id;
        private final CharSequence text;
        private final String time;
        private final boolean isOut;

        public TransactionItem(String id, CharSequence text, String time, boolean isOut) {
            this.id = id;
            this.text = text;
            this.time = time;
            this.isOut = isOut;
        }

        public CharSequence getText() {
            return text;
        }

        public String getTime() {
            return time;
        }

        public boolean isOut() {
            return isOut;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TransactionItem that = (TransactionItem) o;

            if (isOut != that.isOut) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (text != null ? !text.equals(that.text) : that.text != null) return false;
            return time != null ? time.equals(that.time) : that.time == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (text != null ? text.hashCode() : 0);
            result = 31 * result + (time != null ? time.hashCode() : 0);
            result = 31 * result + (isOut ? 1 : 0);
            return result;
        }

        @Override
        public long adapterId() {
            return id.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof TransactionItem && ((TransactionItem) baseAdapterItem).id.equals(id);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem.equals(this);
        }
    }
}
