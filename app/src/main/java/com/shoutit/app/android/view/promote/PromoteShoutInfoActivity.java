package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import com.shoutit.app.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PromoteShoutInfoActivity extends AppCompatActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, PromoteShoutInfoActivity.class);
    }

    @Bind(R.id.promote_shout_info_toolbar)
    Toolbar mConversationToolbar;
    @Bind(R.id.promote_shout_info_footer)
    TextView mPromoteShoutInfoFooter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote_shout_info);
        ButterKnife.bind(this);

        mConversationToolbar.setTitle(R.string.promote_shout_info_title);
        mConversationToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mConversationToolbar.setNavigationOnClickListener(view -> finish());

        mPromoteShoutInfoFooter.setText(Html.fromHtml(getString(R.string.promote_shout_info_footer)));
    }
}