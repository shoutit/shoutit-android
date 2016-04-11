package com.shoutit.app.android.utils.rx;

import android.view.View;
import android.widget.AdapterView;


public class OnItemClickEvent {

    public AdapterView<?> parent;
    public View view;
    public int position;
    public long id;

    public static OnItemClickEvent create(AdapterView<?> parent, View view, int position, long id) {
        return new OnItemClickEvent(parent, view, position, id);
    }

    private OnItemClickEvent(AdapterView<?> parent, View view, int position, long id) {
        this.parent = parent;
        this.view = view;
        this.position = position;
        this.id = id;
    }
}
