package com.shoutit.app.android.view.postlogininterest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupSecondActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

public class PostSignUpInterestsFragment extends BaseDaggerFragment {

    @Bind(R.id.post_signup_interests_recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    PostSignUpInterestsAdapter adapter;
    @Inject
    PostSignupInterestsPresenter presenter;
    
    public static Fragment newInstance() {
        return new PostSignUpInterestsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_signup_interests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);

        presenter
                .getCategoriesItems()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getSuccessCategoriesObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    startActivity(PostSignupSecondActivity.newIntent(getActivity()));
                });

        presenter.getPostCategoriesError()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.hasAnySelectedCategoriesObservable()
                .compose(bindToLifecycle())
                .subscribe(hasAnySelectedCategories -> {
                    ((PostSignUpActivity) getActivity()).enableNextButton(hasAnySelectedCategories);
                });

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
