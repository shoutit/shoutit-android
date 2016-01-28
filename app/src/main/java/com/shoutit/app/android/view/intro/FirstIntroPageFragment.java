package com.shoutit.app.android.view.intro;

import com.shoutit.app.android.R;

public class FirstIntroPageFragment extends IntroFragment {

    @Override
    protected int getLogoResId() {
        return R.drawable.ic_logo;
    }

    @Override
    protected int getFirstTextResId() {
        return R.string.intro_welcome;
    }

    @Override
    protected int getSecondTextResId() {
        return R.string.intro_buy;
    }
}
