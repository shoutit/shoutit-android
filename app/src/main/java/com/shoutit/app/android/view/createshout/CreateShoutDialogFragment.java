package com.shoutit.app.android.view.createshout;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.createshout.request.CreateRequestActivity;
import com.shoutit.app.android.view.createshout.request.CreateShoutDialogComponent;
import com.shoutit.app.android.view.createshout.request.DaggerCreateShoutDialogComponent;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.MainActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateShoutDialogFragment extends DialogFragment {

    @NonNull
    public static DialogFragment newInstance() {
        return new CreateShoutDialogFragment();
    }

    @Inject
    UserPreferences mUserPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final CreateShoutDialogComponent component = DaggerCreateShoutDialogComponent.builder()
                .activityModule(new ActivityModule((AppCompatActivity) getActivity()))
                .appComponent(App.getAppComponent(getActivity().getApplication()))
                .build();

        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_shout_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.create_shout_offer)
    public void createShout() {
        if (mUserPreferences.isGuest()) {
            startActivity(LoginIntroActivity.newIntent(getActivity()));
        } else {
            if (PermissionHelper.checkPermissions(getActivity(),
                    MainActivity.REQUST_CODE_CAMERA_PERMISSION,
                    ColoredSnackBar.contentView(getActivity()),
                    R.string.permission_camera_explanation,
                    new String[] {Manifest.permission.CAMERA})) {
                startActivity(NativeCameraActivity.newIntent(getActivity()));
            }
        }
    }

    @OnClick(R.id.create_shout_request)
    public void createRequest() {
        if (mUserPreferences.isGuest()) {
            startActivity(LoginIntroActivity.newIntent(getActivity()));
        } else {
            startActivity(CreateRequestActivity.newIntent(getActivity()));
        }
    }
}
