package com.shoutit.app.android.view.conversations;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;

public class ConversationsPagerFragment extends BaseFragment {

    private static final String ARG_SHOW_PUBLIC_CHATS = "show_public_chats";

    @Bind(R.id.conversations_pager_tablayout)
    TabLayout tabLayout;
    @Bind(R.id.conversations_pager_view_pager)
    ViewPager viewPager;

    @Inject
    ConversationsPagerAdapter adapter;

    @Nullable
    private View mLogo;
    private List<MenuItem> mMenuItems = Lists.newArrayList();

    public static Fragment newInstance() {
        return new ConversationsPagerFragment();
    }

    public static Fragment newInstance(boolean showPublicChats) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_SHOW_PUBLIC_CHATS, showPublicChats);

        final ConversationsPagerFragment fragment = new ConversationsPagerFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.conversation_title);
        mLogo = activity.findViewById(R.id.activity_main_logo);
        if (mLogo != null) {
            mLogo.setVisibility(View.GONE);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                getActivity().invalidateOptionsMenu();

            }
        });

        if (getArguments() != null) {
            final boolean showPublicChats = getArguments().getBoolean(ARG_SHOW_PUBLIC_CHATS);
            if (showPublicChats) {
                viewPager.setCurrentItem(ConversationsPagerAdapter.POSITION_PUBLIC_CONVERSATIONS);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLogo != null) {
            mLogo.setVisibility(View.VISIBLE);
        }
        final BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(null);
        for (MenuItem item : mMenuItems) {
            item.setVisible(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversations_menu, menu);

        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            item.setVisible(false);
            mMenuItems.add(item);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.conversations_menu_create_public_chat);
        item.setVisible(viewPager.getCurrentItem() == ConversationsPagerAdapter.POSITION_PUBLIC_CONVERSATIONS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.conversations_menu_create_public_chat:
                // TODO create public chat
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        final ConversationsPagerFragmentComponent component = DaggerConversationsPagerFragmentComponent
                .builder()
                .fragmentModule(new FragmentModule(this))
                .baseActivityComponent(baseActivityComponent)
                .build();
        component.inject(this);
    }
}
