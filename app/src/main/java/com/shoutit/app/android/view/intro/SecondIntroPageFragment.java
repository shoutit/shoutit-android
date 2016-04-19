package com.shoutit.app.android.view.intro;

import com.shoutit.app.android.R;


public class SecondIntroPageFragment extends IntroFragment {

    @Override
    protected int getLogoResId() {
        return R.drawable.ic_intro_search;
    }

    @Override
    protected int getFirstTextResId() {
        return R.string.intro_search;
    }

    @Override
    protected int getSecondTextResId() {
        return R.string.intro_search_text;
    }
}
