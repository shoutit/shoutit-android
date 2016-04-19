package com.shoutit.app.android.view.intro;

import com.shoutit.app.android.R;

public class FifthIntroPageFragment extends IntroFragment {

    @Override
    protected int getLogoResId() {
        return R.drawable.ic_intro_video;
    }

    @Override
    protected int getFirstTextResId() {
        return R.string.intro_video;
    }

    @Override
    protected int getSecondTextResId() {
        return R.string.intro_video_text;
    }
}



