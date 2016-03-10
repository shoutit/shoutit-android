package com.shoutit.app.android.view.postlogininterest.postsignupsecond;


import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.R;

import java.util.List;

import rx.Observable;

public class PostSignupUserFragment extends PostSignupSecondFragment {

    public static PostSignupUserFragment newInstance() {
        return new PostSignupUserFragment();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.post_signup_suggested_users);
    }

    @Override
    protected Observable<List<BaseAdapterItem>> getAdapterItems() {
        return presenter.getSuggestedUsersObservable();
    }
}
