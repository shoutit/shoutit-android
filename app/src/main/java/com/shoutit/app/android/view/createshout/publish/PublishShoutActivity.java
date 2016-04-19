package com.shoutit.app.android.view.createshout.publish;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.view.createshout.edit.EditShoutActivity;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublishShoutActivity extends RxAppCompatActivity {

    private static final String ARGS_REQUEST = "args_request";
    private static final String ARGS_ID = "args_id";
    private static final String ARGS_WEB_URL = "web_url";
    private static final String ARGS_TITLE = "args_title";

    private boolean mRequest;
    private String mId;
    private String mWebUrl;

    @Bind(R.id.publish_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.publish_subheader)
    TextView subHeader;

    @Bind(R.id.publish_activity_create_another_shout)
    Button button;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String id, @NonNull String webUrl, boolean request, @Nullable String title) {
        return new Intent(context, PublishShoutActivity.class)
                .putExtra(ARGS_ID, id)
                .putExtra(ARGS_REQUEST, request)
                .putExtra(ARGS_TITLE, title)
                .putExtra(ARGS_WEB_URL, webUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_shout_activity);

        ButterKnife.bind(this);

        mToolbar.inflateMenu(R.menu.publish_shout_menu);
        mToolbar.setNavigationIcon(R.drawable.close);

        final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_share);
        assert drawable != null;
        final Drawable newDrawable = drawable.mutate();

        newDrawable.setColorFilter(getResources().getColor(R.color.black_54), PorterDuff.Mode.SRC_IN);
        mToolbar.getMenu().findItem(R.id.publish_share).setIcon(newDrawable);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.publish_share: {
                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("text/plain");

                        share.putExtra(Intent.EXTRA_TEXT, mWebUrl);

                        startActivity(Intent.createChooser(share, "Share link!"));
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolbar.setTitle(null);

        final Bundle extras = getIntent().getExtras();
        mRequest = extras.getBoolean(ARGS_REQUEST);
        mId = extras.getString(ARGS_ID);
        mWebUrl = extras.getString(ARGS_WEB_URL);

        final String title = extras.getString(ARGS_TITLE);
        if (Strings.isNullOrEmpty(title)) {
            subHeader.setText(getString(R.string.published_extra_info,
                    getString(mRequest ? R.string.publish_request : R.string.publish_offer)));
        } else {
            subHeader.setText(getString(R.string.published_extra_info_with_title,
                    getString(mRequest ? R.string.publish_request : R.string.publish_offer), title));
        }

        button.setText(getString(R.string.published_create_another_shout,
                getString(mRequest ? R.string.publish_request_capitalized : R.string.publish_offer_capitalized)));
    }

    @OnClick(R.id.publish_activity_create_another_shout)
    public void createAnotherShout() {
        if (mRequest) {
            startActivity(CreateRequestActivity.newIntent(this));
        } else {
            startActivity(RecordMediaActivity.newIntent(this, false, false, false, true));
        }
        finish();
    }

    @OnClick(R.id.publish_activity_add_more_details)
    public void moreDetails() {
        startActivity(EditShoutActivity.newIntent(mId, this));
        finish();
    }
}
