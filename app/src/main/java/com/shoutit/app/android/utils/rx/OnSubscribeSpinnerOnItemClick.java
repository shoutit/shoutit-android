package com.shoutit.app.android.utils.rx;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;


public class OnSubscribeSpinnerOnItemClick implements Observable.OnSubscribe<OnItemClickEvent> {

    private final Spinner spinner;

    public OnSubscribeSpinnerOnItemClick(final Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public void call(final Subscriber<? super OnItemClickEvent> observer) {
        Assertions.assertUiThread();
        final CompositeOnClickListener composite = CachedListeners.getFromViewOrCreate(spinner);

        final Spinner.OnItemSelectedListener listener = new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    observer.onNext(OnItemClickEvent.create(parent, view, position, id));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeListener(listener);
            }
        });

        composite.addListener(listener);
        observer.add(subscription);
    }

    private static class CompositeOnClickListener implements Spinner.OnItemSelectedListener {
        private final List<Spinner.OnItemSelectedListener> listeners = new ArrayList<>();

        public boolean addListener(final Spinner.OnItemSelectedListener listener) {
            return listeners.add(listener);
        }

        public boolean removeListener(final Spinner.OnItemSelectedListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            for (Spinner.OnItemSelectedListener listener : listeners) {
                listener.onItemSelected(parent, view, position, id);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static class CachedListeners {
        private static final Map<Spinner, CompositeOnClickListener> sCachedListeners = new WeakHashMap<>();

        public static CompositeOnClickListener getFromViewOrCreate(final Spinner view) {
            final CompositeOnClickListener cached = sCachedListeners.get(view);

            if (cached != null) {
                return cached;
            }

            final CompositeOnClickListener listener = new CompositeOnClickListener();

            sCachedListeners.put(view, listener);
            view.setOnItemSelectedListener(listener);

            return listener;
        }
    }
}

