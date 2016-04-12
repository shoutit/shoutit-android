package com.shoutit.app.android.view.conversations;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.collect.Lists;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.main.MainActivityComponent;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;

public class ConverstationsFragment extends BaseFragment implements ConversationsPresenter.Listener {

    @Bind(R.id.conversation_recyclerview)
    RecyclerView mConversationRecyclerview;
    @Bind(R.id.conversation_progress)
    ProgressBar mConversationProgress;
    @Bind(R.id.conversation_empty)
    TextView mConversationEmptyText;

    @Inject
    ConversationsPresenter presenter;
    @Inject
    ConversationsAdapter adapter;

    private View mLogo;
    private List<MenuItem> mItems = Lists.newArrayList();

    public static Fragment newInstance() {
        return new ConverstationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_conversations, container, false);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @javax.annotation.Nullable Bundle savedInstanceState) {
        final ConversationsActivityComponent component = DaggerConversationsActivityComponent
                .builder()
                .fragmentModule(new FragmentModule(this))
                .mainActivityComponent((MainActivityComponent) baseActivityComponent)
                .build();
        component.inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversationRecyclerview.setAdapter(adapter);
        mConversationRecyclerview.setLayoutManager(new MyLinearLayoutManager(getActivity()));

        final MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.conversation_title);
        mLogo = activity.findViewById(R.id.activity_main_logo);
        mLogo.setVisibility(View.GONE);

        RxRecyclerView.scrollEvents(mConversationRecyclerview)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mConversationRecyclerview.getLayoutManager(), adapter))
                .subscribe(presenter.loadMoreObserver());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unregister();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLogo.setVisibility(View.VISIBLE);
        final MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(null);
        for (MenuItem item : mItems) {
            item.setVisible(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            item.setVisible(false);
            mItems.add(item);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void emptyList() {
        mConversationEmptyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgress(boolean show) {
        mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setData(@NonNull List<BaseAdapterItem> items) {
        adapter.call(items);
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(@NonNull String id, boolean shoutChat) {
        startActivity(ChatActivity.newIntent(getActivity(), id, shoutChat));
    }
}