package com.shoutit.app.android.view.intro;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shoutit.app.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public abstract class IntroFragment extends Fragment {

    @Bind(R.id.activity_intro_logo)
    ImageView logoImageView;
    @Bind(R.id.activity_intro_first_text)
    TextView firstTextView;
    @Bind(R.id.activity_intro_second_text)
    TextView secondTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        bindData();
    }

    private void bindData() {
        logoImageView.setImageDrawable(getResources().getDrawable(getLogoResId()));
        firstTextView.setText(getFirstTextResId());
        secondTextView.setText(getSecondTextResId());
    }

    @DrawableRes
    protected abstract int getLogoResId();
    @StringRes
    protected abstract int getFirstTextResId();
    @StringRes
    protected abstract int getSecondTextResId();

}
