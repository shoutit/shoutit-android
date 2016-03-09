package com.shoutit.app.android.view.createshout.publish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.shoutit.app.android.R;
import com.shoutit.app.android.view.createshout.edit.EditShoutActivity;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublishShoutActivity extends RxAppCompatActivity {

    private static final String ARGS_REQUEST = "args_request";
    private static final String ARGS_ID = "args_id";
    private boolean mRequest;
    private String mId;

    @Bind(R.id.publish_activity_toolbar)
    Toolbar mToolbar;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String id, boolean request) {
        return new Intent(context, PublishShoutActivity.class)
                .putExtra(ARGS_ID, id)
                .putExtra(ARGS_REQUEST, request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_shout_activity);

        ButterKnife.bind(this);

        mToolbar.inflateMenu(R.menu.publish_shout_menu);
        mToolbar.setNavigationIcon(R.drawable.close);

        final Drawable drawable = getResources().getDrawable(R.drawable.ic_share);
        assert drawable != null;
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        mToolbar.getMenu().findItem(R.id.publish_share).setIcon(drawable);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolbar.setTitle(null);

        mRequest = getIntent().getExtras().getBoolean(ARGS_REQUEST);
        mId = getIntent().getExtras().getString(ARGS_ID);

    }

    @OnClick(R.id.publish_activity_create_another_shout)
    public void createAnotherShout() {
        if (mRequest) {
            startActivity(CreateRequestActivity.newIntent(this));
        } else {
            // TODO start create shout
        }
        finish();
    }

    @OnClick(R.id.publish_activity_add_more_details)
    public void moreDetails() {
        startActivity(EditShoutActivity.newIntent(mId, this));
        finish();
    }
}
