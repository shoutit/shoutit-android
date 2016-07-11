package com.shoutit.app.android.view.verifybusiness;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseEmptyActivityComponent;
import com.shoutit.app.android.dagger.DaggerBaseEmptyActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.squareup.picasso.Picasso;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerifyBusinessActivity extends BaseActivity implements VerifyBusinessPresenter.Listener {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_IMAGE_PICK = 2;

    @Bind(R.id.business_ver_name_et)
    EditText nameEt;
    @Bind(R.id.business_ver_name_il)
    TextInputLayout nameTil;

    @Bind(R.id.business_ver_contact_person_et)
    EditText contactPersonEt;
    @Bind(R.id.business_ver_contact_person_tl)
    TextInputLayout contactPersonTil;

    @Bind(R.id.business_ver_contact_number_et)
    EditText contactNumberEt;
    @Bind(R.id.business_ver_contact_number_tl)
    TextInputLayout contactNumberTil;

    @Bind(R.id.business_ver_email_et)
    EditText emailEt;
    @Bind(R.id.business_ver_email_til)
    TextInputLayout emailTil;

    @Bind(R.id.business_ver_images_container)
    LinearLayout imagesContainer;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.business_ver_toolbar)
    Toolbar toolbar;

    @Inject
    VerifyBusinessPresenter presenter;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper imageCaptureHelper;
    @Inject
    EditImageDialog editImageDialog;

    public static Intent newIntent(Context context) {
        return new Intent(context, VerifyBusinessActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buisness_verification);
        ButterKnife.bind(this);

        setupToolbar();

        presenter.register(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.business_ver_ab_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.business_ver_confirm_btn)
    public void onConfirmClick() {
        presenter.submitForm(nameEt.getText().toString(),
                contactPersonEt.getText().toString(),
                contactNumberEt.getText().toString(),
                emailEt.getText().toString());
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final BaseEmptyActivityComponent component = DaggerBaseEmptyActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }

    @Override
    protected void onDestroy() {
        presenter.unregister();
        super.onDestroy();
    }

    @Override
    public void setImages(@NonNull Map<Integer, VerifyBusinessPresenter.Item> mediaElements) {
        imagesContainer.removeAllViews();

        for (int i = 0; i < mediaElements.size(); i++) {
            final VerifyBusinessPresenter.Item item = mediaElements.get(i);

            final View view;

            if (item instanceof VerifyBusinessPresenter.AddImageItem) {
                view = layoutInflater.inflate(R.layout.edit_media_add, imagesContainer, false);
            } else if (item instanceof VerifyBusinessPresenter.ImageItem) {
                view = layoutInflater.inflate(R.layout.edit_media_item, imagesContainer, false);
                final ImageView imageView = (ImageView) view.findViewById(R.id.edit_media_item_image);
                picasso.load(Uri.parse(((VerifyBusinessPresenter.ImageItem) item).getMedia()))
                        .centerCrop()
                        .fit()
                        .into(imageView);
            } else if (item instanceof VerifyBusinessPresenter.BlankItem) {
                view = layoutInflater.inflate(R.layout.edit_media_blank, imagesContainer, false);
            } else {
                throw new RuntimeException("Unknown item " + item.getClass().getSimpleName());
            }

            view.setOnClickListener(v -> item.click());
            imagesContainer.addView(view);
        }
    }

    private void captureImage() {
        if (!PermissionHelper.checkPermissions(this, REQUEST_CODE_PERMISSION, ColoredSnackBar.contentView(this),
                R.string.permission_camera_create_shout_explanation,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})) {
            return;
        }

        final Optional<Intent> captureIntent = imageCaptureHelper.createSelectOrCaptureImageIntent();
        if (captureIntent.isPresent()) {
            startActivityForResult(captureIntent.get(), REQUEST_CODE_IMAGE_PICK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                captureImage();
            } else {
                ColoredSnackBar.error(ColoredSnackBar.contentView(this),
                        R.string.permission_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Optional<Uri> uri;

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            uri = imageCaptureHelper.onResult(resultCode, data);
            if (uri.isPresent()) {
                presenter.uploadToAmazonAndAddItem(uri.get());
            } else {
                showError(getString(R.string.error_image_upload));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void startImageChooser(int position) {
        presenter.startImageChooser(position);
    }

    @Override
    public void showProgress() {
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressView.setVisibility(View.GONE);
    }

    @Override
    public void showImageDialog(int position) {
        editImageDialog.show(position, presenter);
    }

    @Override
    public void startImageChooser() {
        captureImage();
    }

    @Override
    public void showError(Throwable throwable) {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), throwable).show();
    }

    @Override
    public void showError(String message) {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSuccessAndFinish() {
        Toast.makeText(this, R.string.business_ver_sucess, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void showNameError(String error) {
        nameTil.setError(error);
    }

    @Override
    public void showPersonError(String error) {
        contactPersonTil.setError(error);
    }

    @Override
    public void showNumberError(String error) {
        contactNumberTil.setError(error);
    }

    @Override
    public void showEmailError(String error) {
        emailTil.setError(error);
    }

}
