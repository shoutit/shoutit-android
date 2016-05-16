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
        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            item.setVisible(false);
            mMenuItems.add(item);
        }
        super.onCreateOptionsMenu(menu, inflater);
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
