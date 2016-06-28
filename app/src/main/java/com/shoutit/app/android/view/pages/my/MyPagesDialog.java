package com.shoutit.app.android.view.pages.my;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MyPagesDialog {

    private final Context mContext;

    @Bind(R.id.my_pages_view_page_btn)
    Button viewPageBtn;
    @Bind(R.id.my_pages_use_shoutit_as_this_page_btn)
    Button useShoutItAsPageBtn;
    @Bind(R.id.my_pages_edit_page_btn)
    Button editPageBtn;

    @Inject
    public MyPagesDialog(@ForActivity Context context) {
        mContext = context;
    }

    public void show(@Nonnull Page page, PagesDialogListener listener) {

        final View view = LayoutInflater.from(mContext).inflate(R.layout.my_pages_dialog, null, false);

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(page.getName())
                .setView(view)
                .create();

        ButterKnife.bind(this, view);

        viewPageBtn.setOnClickListener(v -> {
            listener.showProfile(page.getUsername());
            alertDialog.dismiss();
        });

        useShoutItAsPageBtn.setOnClickListener(v -> {
            listener.useShoutItAsPage(page.getUsername(), page.getId());
            alertDialog.dismiss();
        });

        editPageBtn.setOnClickListener(v -> {
            listener.editPage(page.getUsername());
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public interface PagesDialogListener {

        void showProfile(String userName);

        void editPage(String userName);

        void useShoutItAsPage(Page page);

    }
}

