package com.shoutit.app.android.view.verifybusiness;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class EditImageDialog {

    private final Activity mContext;

    @Bind(R.id.edit_image_change)
    Button changeImageButton;
    @Bind(R.id.edit_image_delete)
    Button deleteImageButton;

    @Inject
    public EditImageDialog(@ForActivity Context context) {
        mContext = (Activity) context;
    }

    public void show(int position, final VerifyBusinessPresenter presenter) {

        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit_image, null, false);

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.shout_media_edit_dialog_title))
                .setView(view)
                .create();
        ButterKnife.bind(this, view);

        changeImageButton.setOnClickListener(v -> {
            presenter.startImageChooser(position);
            alertDialog.dismiss();
        });

        deleteImageButton.setOnClickListener(v -> {
            presenter.removeItem(position);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

}

