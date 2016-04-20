package com.shoutit.app.android.view.intro;

import com.shoutit.app.android.R;

public class FourthIntroPageFragment extends IntroFragment {

    @Override
    protected int getLogoResId() {
        return R.drawable.ic_intro_chat;
    }

    @Override
    protected int getFirstTextResId() {
        return R.string.intro_chat;
    }

    @Override
    protected int getSecondTextResId() {
        return R.string.intro_chat_text;
    }
}


