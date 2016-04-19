package com.shoutit.app.android.view.intro;

import com.shoutit.app.android.R;

public class ThirdIntroPageFragment extends IntroFragment {

    @Override
    protected int getLogoResId() {
        return R.drawable.ic_intro_shout;
    }

    @Override
    protected int getFirstTextResId() {
        return R.string.intro_shout;
    }

    @Override
    protected int getSecondTextResId() {
        return R.string.intro_shout_text;
    }
}

