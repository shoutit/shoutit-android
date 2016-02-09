package com.shoutit.app.android.view.postlogininterest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class PostLoginInterestActivity extends BaseActivity {

    @Bind(R.id.post_login_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.post_login_toolbar)
    Toolbar mToolbar;

    @Inject
    PostLoginAdapter mPostLoginAdapter;

    @Inject
    PostLoginPresenter mPostLoginPresenter;

    @Nonnull
    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, PostLoginInterestActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_login_activity);

        ButterKnife.bind(this);

        mToolbar.inflateMenu(R.menu.post_login_menu);
        mToolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mPostLoginAdapter);

        mPostLoginPresenter
                .getCategoriesList()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mPostLoginAdapter);

        mPostLoginPresenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this), R.string.post_login_error));

        mPostLoginPresenter.getSuccessCategoriesObservable()
                .compose(bindToLifecycle())
                .subscribe(getFinishActivity());

        mPostLoginPresenter.getPostCategoriesError()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this), R.string.post_login_send_error));

        RxToolbarMore.menuClick(mToolbar)
                .filter(RxToolbarMore.filterMenuClick(R.id.post_login_next))
                .compose(bindToLifecycle())
                .subscribe(mPostLoginPresenter.nextClickedObserver());

        RxToolbarMore.navigationClick(mToolbar)
                .compose(bindToLifecycle())
                .subscribe(getFinishActivity());
    }

    @NonNull
    private Action1<? super Object> getFinishActivity() {
        return new Action1<Object>() {
            @Override
            public void call(Object view) {
                finish();
            }
        };
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final PostLoginActivityComponent postLoginActivityComponent = DaggerPostLoginActivityComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .activityModule(new ActivityModule(this))
                .build();
        postLoginActivityComponent.inject(this);
        return postLoginActivityComponent;
    }
}
