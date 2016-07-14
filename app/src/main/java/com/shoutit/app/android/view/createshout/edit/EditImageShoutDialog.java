package com.shoutit.app.android.view.createshout.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.createshout.ShoutMediaPresenter;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

public class EditImageShoutDialog {

    private final Activity mContext;
    @Bind(R.id.shout_media_edit_dialog_edit)
    Button mShoutMediaEditDialogEdit;
    @Bind(R.id.shout_media_edit_dialog_change)
    Button mShoutMediaEditDialogChange;
    @Bind(R.id.shout_media_edit_dialog_delete)
    Button mShoutMediaEditDialogDelete;

    @Inject
    public EditImageShoutDialog(@ForActivity Context context) {
        mContext = (Activity) context;
    }

    public void show(int position, final ShoutMediaPresenter presenter, PublishSubject<Pair<String, Boolean>> mediaSwappedSubject, String path) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shout_image_edit_dialog, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.shout_media_edit_dialog_title))
                .setView(view)
                .create();
        ButterKnife.bind(this, view);

        mShoutMediaEditDialogEdit.setOnClickListener(v -> {
            mediaSwappedSubject
                    .take(1)
                    .subscribe(stringBooleanPair -> {
                        presenter.swapMediaItem(position, stringBooleanPair.first, stringBooleanPair.second);
                    });
            final Intent imageEditorIntent = new AdobeImageIntent.Builder(mContext)
                    .setData(Uri.parse(path))
                    .build();
            mContext.startActivityForResult(imageEditorIntent, EditShoutActivity.MEDIA_EDIT_EDTOR_REQUEST_CODE);
            alertDialog.dismiss();
        });

        mShoutMediaEditDialogChange.setOnClickListener(v -> {
            mediaSwappedSubject
                    .take(1)
                    .subscribe(stringBooleanPair -> {
                        presenter.swapMediaItem(position, stringBooleanPair.first, stringBooleanPair.second);
                    });
            mContext.startActivityForResult(RecordMediaActivity.newIntent(mContext, true, false, false, false, true), EditShoutActivity.MEDIA_EDIT_REQUEST_CODE);
            alertDialog.dismiss();
        });

        mShoutMediaEditDialogDelete.setOnClickListener(v -> {
            presenter.removeItem(position);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

}
