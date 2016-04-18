package com.shoutit.app.android.view.chats;

import android.view.MotionEvent;
import android.view.View;

public class ChatsHelper {

    public static void setOnClickHideListener(View viewToSetListener, final View viewTohide){
        viewToSetListener.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewTohide.setVisibility(View.GONE);
                return false;
            }
        });
    }
}
