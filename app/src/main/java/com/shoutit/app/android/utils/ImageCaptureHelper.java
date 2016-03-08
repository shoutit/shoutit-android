package com.shoutit.app.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImageCaptureHelper {
    private final Context mContext;
    private Uri lastImage;

    @Inject
    public ImageCaptureHelper(@ForActivity Context context) {
        mContext = context;
    }

    private Uri createImageUri() {
        final ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis());
        final ContentResolver contentResolver = mContext.getContentResolver();
        return contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
    }

    public Optional<Intent> createCaptureImageIntent() {
        if (displayErrorIfExternalSotrageNotMounted()) return Optional.absent();

        final Intent captureIntent = makeImageIntent();
        return Optional.of(captureIntent);
    }


    private Intent makeImageIntent() {
        lastImage = createImageUri();

        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, lastImage);
    }

    public Optional<Intent> createSelectOrCaptureImageIntent() {
        if (displayErrorIfExternalSotrageNotMounted()) return Optional.absent();

        final Intent captureIntent = makeImageIntent();

        final List<Intent> additionalIntents = getIntents(captureIntent);

        final Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                .setType("image/*");

        final String title = mContext.getString(R.string.image_video_capture_helper_select_image_source);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, title);

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                additionalIntents.toArray(new Parcelable[additionalIntents.size()]));
        return Optional.of(chooserIntent);
    }

    private boolean displayErrorIfExternalSotrageNotMounted() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            new AlertDialog.Builder(mContext)
                    .setMessage(R.string.image_video_capture_helper_sd_unmounted)
                    .setTitle(R.string.image_video_capture_helper_sd_unmounted_title)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();

                                }
                            }).show();
            return true;
        }
        return false;
    }

    public Optional<Uri> onResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                lastImage = intent.getData();
            }
            return Optional.fromNullable(lastImage);
        } else {
            if (lastImage != null) {
                mContext.getContentResolver().delete(lastImage, null, null);
            }
        }

        lastImage = null;
        return Optional.absent();
    }

    @Nonnull
    private ImmutableList<Intent> getIntents(@Nonnull Intent intent) {
        final PackageManager packageManager = checkNotNull(mContext.getPackageManager());
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        final List<Intent> intents = new ArrayList<>();
        for (ResolveInfo res : resolveInfos) {
            final ActivityInfo activityInfo = res.activityInfo;
            if (activityInfo == null) {
                continue;
            }
            intents.add(new Intent(intent)
                    .setClassName(activityInfo.packageName, activityInfo.name));
        }
        return ImmutableList.copyOf(intents);
    }

    public void onCreate(@Nullable Bundle savedInstanceState, @Nonnull String key) {
        if (savedInstanceState != null) {
            lastImage = savedInstanceState.getParcelable(key);
        } else {
            lastImage = null;
        }
    }

    public void onSaveInstanceState(Bundle outState, String key) {
        outState.putParcelable(key, lastImage);
    }

    public Uri getLastImage() {
        return lastImage;
    }
}
