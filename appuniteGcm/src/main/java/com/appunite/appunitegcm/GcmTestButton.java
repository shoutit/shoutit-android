package com.appunite.appunitegcm;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatButton;

public class GcmTestButton extends AppCompatButton {

    private GcmPushHandler gcmPushHandler;

    public GcmTestButton(Context context) {
        this(context, null);
    }

    public GcmTestButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GcmTestButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        handleSendRequest();
        return super.performClick();
    }

    private void handleSendRequest() {
        if (gcmPushHandler == null) {
            throw new IllegalStateException("GcmPushHandler is not set. " +
                    "Use registerGcmPushHandler(GcmPushHandler gcmPushHandler).");
        }
        final String gcmPushData = gcmPushHandler.getGcmPushData();
        final String serverKey = gcmPushHandler.getServerKey();
        if (serverKey == null) {
            throw new NullPointerException("Server key is null");
        }
        if (gcmPushData == null) {
            throw new NullPointerException("Gcm push data is null");
        }

        AppuniteGcm.getInstance()
                .getSendPushObserver()
                .onNext(new GcmPushDataWithServerKey(serverKey, gcmPushData));
    }

    public void registerGcmPushHandler(GcmPushHandler gcmPushHandler) {
        this.gcmPushHandler = gcmPushHandler;
    }

    public void unregisterGcmPushHandler() {
        this.gcmPushHandler = null;
    }
}
