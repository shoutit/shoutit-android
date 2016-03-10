package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.R;

import java.util.List;

import rx.Observable;

public class PostSignupPagesFragment extends PostSignupSecondFragment {

    public static PostSignupPagesFragment newInstance() {
        return new PostSignupPagesFragment();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.post_signup_suggested_pages);
    }

    @Override
    protected Observable<List<BaseAdapterItem>> getAdapterItems() {
        return presenter.getSuggestedPagesObservable();
    }
}
