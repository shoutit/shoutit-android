package com.shoutit.app.android.view.postsignup.users;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.utils.ApiMessagesHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MyLinearLayoutManager;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;

public class PostSignUpUsersFragment extends BaseDaggerFragment {

    @Inject
    PostSignUpUserPresenter presenter;
    @Inject
    PostSignUpUsersAdapter adapter;

    @Bind(R.id.post_signup_users_recyclerview)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    public static Fragment newInstance() {
        return new PostSignUpUsersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_signup_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapter();

        presenter.getSuggestedUsersObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(getActivity()));

        presenter.getUnListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(getActivity()));

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));
    }

    private void setupAdapter() {
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
