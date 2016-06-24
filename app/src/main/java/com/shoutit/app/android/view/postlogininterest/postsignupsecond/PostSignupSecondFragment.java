package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.rx.RxUtils;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;

public abstract class PostSignupSecondFragment extends BaseFragment {

    @Inject
    PostSignupSecondPresenter presenter;
    @Inject
    PostSignupSecondAdapter adapter;

    @Bind(R.id.post_signup_second_recyclerview)
    RecyclerView recyclerView;
    @Bind(R.id.post_signup_second_title_tv)
    TextView titleTv;
    @Bind(R.id.placeholder)
    TextView placeholderTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.post_signup_second_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapter();

        titleTv.setText(getTitle());

        getAdapterItems()
                .filter(List::isEmpty)
                .map(Functions1.returnTrue())
                .subscribe(isEmpty -> {
                    placeholderTv.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });

        getAdapterItems()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        RxTextView.textChangeEvents(titleTv)
                .compose(bindToLifecycle())
                .map(textViewTextChangeEvent -> getString(R.string.no_suggested_pages))
                .subscribe(RxTextView.text(placeholderTv));

        presenter.getListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.listenMessageAction(getActivity()));

        presenter.getUnListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.unListenMessageAction(getActivity()));
    }

    private void setupAdapter() {
        recyclerView.setLayoutManager(new NoScrollLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    protected abstract String getTitle();

    protected abstract Observable<List<BaseAdapterItem>> getAdapterItems();

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerPostignupSecondFragmentComponent
                .builder()
                .baseActivityComponent(baseActivityComponent)
                .postSignupPresenterComponent((PostSignupPresenterComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

    public class NoScrollLayoutManager extends LinearLayoutManager {

        public NoScrollLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }
}
