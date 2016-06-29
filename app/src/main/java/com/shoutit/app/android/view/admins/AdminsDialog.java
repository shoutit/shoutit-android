package com.shoutit.app.android.view.admins;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class AdminsDialog {

    private final Context context;

    @Bind(R.id.admin_view_profile_btn)
    Button viewProfileBtn;
    @Bind(R.id.admin_remove_admin_btn)
    Button removeAdminBtn;

    @Inject
    public AdminsDialog(@ForActivity Context context) {
        this.context = context;
    }

    public void show(@Nonnull BaseProfile baseProfile, AdminsDialogListener listener) {

        final View view = LayoutInflater.from(context).inflate(R.layout.admins_dialog, null, false);

        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        ButterKnife.bind(this, view);

        viewProfileBtn.setOnClickListener(v -> {
            listener.showProfile(baseProfile.getUsername());
            alertDialog.dismiss();
        });

        removeAdminBtn.setOnClickListener(v -> {
            listener.removeAdmin(baseProfile.getId());
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public interface AdminsDialogListener {

        void showProfile(String userName);

        void removeAdmin(String selectedAdminId);
    }
}


