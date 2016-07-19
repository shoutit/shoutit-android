package com.shoutit.app.android.view.conversations;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
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
import com.shoutit.app.android.view.main.MainActivityComponent;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;

public class ConversationsFragment extends BaseFragment implements ConversationsPresenter.Listener {

    private static final String KEY_IS_MY_CONVERSATIONS = "is_my_conversations";

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

    public static Fragment newInstance(final boolean isMyConversations) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_IS_MY_CONVERSATIONS, isMyConversations);

        final ConversationsFragment fragment = new ConversationsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {

        final boolean isMyConversations = getArguments().getBoolean(KEY_IS_MY_CONVERSATIONS);

        final ConversationsFragmentComponent component = DaggerConversationsFragmentComponent
                .builder()
                .fragmentModule(new FragmentModule(this))
                .converstationsFragmentModule(new ConverstationsFragmentModule(isMyConversations))
                .baseActivityComponent(baseActivityComponent)
                .busComponent((BusComponent) baseActivityComponent)
                .build();
        component.inject(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversationRecyclerview.setAdapter(adapter);
        mConversationRecyclerview.setLayoutManager(new MyLinearLayoutManager(getActivity()));

        RxRecyclerView.scrollEvents(mConversationRecyclerview)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mConversationRecyclerview.getLayoutManager(), adapter))
                .subscribe(presenter.loadMoreObserver());
        presenter.register(this);
    }

    @Override
    public void onDestroyView() {
        presenter.unregister();
        super.onDestroyView();
    }

    @Override
    public void emptyList() {
        mConversationRecyclerview.setVisibility(View.GONE);
        mConversationEmptyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgress(boolean show) {
        mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setData(@NonNull List<BaseAdapterItem> items) {
        mConversationEmptyText.setVisibility(View.GONE);
        mConversationRecyclerview.setVisibility(View.VISIBLE);
        adapter.call(items);
        if (items.size() <= ConversationsPresenter.PAGE_SIZE) {
            mConversationRecyclerview.scrollToPosition(0); // Workaround for scrolled down recyclerview on location changed
        }
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(@NonNull String id, boolean isPublicChat) {
        startActivity(ChatActivity.newIntent(getActivity(), id));
    }
}