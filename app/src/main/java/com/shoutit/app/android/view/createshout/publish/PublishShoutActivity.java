package com.shoutit.app.android.view.createshout.publish;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.shoutit.app.android.R;
import com.shoutit.app.android.view.createshout.edit.EditShoutActivity;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.OnClick;

public class PublishShoutActivity extends RxAppCompatActivity {

    private static final String ARGS_REQUEST = "args_request";
    private static final String ARGS_ID = "args_id";
    private boolean mRequest;
    private String mId;

    @NonNull
    public static Intent newIntent(@NonNull String id, boolean request) {
        return new Intent()
                .putExtra(ARGS_ID, id)
                .putExtra(ARGS_REQUEST, request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_shout_activity);

        mRequest = getIntent().getExtras().getBoolean(ARGS_REQUEST);
        mId = getIntent().getExtras().getString(ARGS_ID);

    }

    @OnClick(R.id.publish_activity_create_another_shout)
    public void createAnotherShout() {
        if (mRequest) {
            startActivity(CreateRequestActivity.newIntent(this));
        }
    }

    @OnClick(R.id.publish_activity_add_more_details)
    public void moreDetails() {
        startActivity(EditShoutActivity.newIntent(mId, this));
    }
}
