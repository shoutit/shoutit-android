package com.shoutit.app.android.view.home.picks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.view.home.myfeed.MyFeedAdapter;
import com.shoutit.app.android.view.home.myfeed.MyFeedPresenter;

import javax.inject.Inject;

import butterknife.Bind;

public class PicksFragment extends BaseDaggerFragment {

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.base_recycler_view)
    RecyclerView recyclerView;

    @Inject
    PicksAdapter adapter;
    @Inject
    PicksPresenter presenter;

    public static Fragment newInstance() {
        return new PicksFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_with_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
