package com.shoutit.app.android.view.intro;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shoutit.app.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class IntroFragment extends Fragment {

    private static final String EXTRA_PAGE_NUMBER = "page_number";

    @Bind(R.id.activity_intro_logo)
    ImageView logoImageView;
    @Bind(R.id.activity_intro_first_text)
    TextView firstTextView;
    @Bind(R.id.activity_intro_second_text)
    TextView secondTextView;

    public static IntroFragment newInstance(int pageNumber) {
        final Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_PAGE_NUMBER, pageNumber);
        final IntroFragment fragment = new IntroFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        checkNotNull(getArguments());
        final int pageNumber = getArguments().getInt(EXTRA_PAGE_NUMBER);

        switch (pageNumber) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                bindData(R.drawable.ic_logo, R.string.intro_welcome, R.string.intro_buy);
                break;
            default:
                throw new RuntimeException("Unknown page size");
        }
    }

    private void bindData(@DrawableRes int logoResId,
                          @StringRes int firstTextId,
                          @StringRes int secondTextId) {
        logoImageView.setImageDrawable(getResources().getDrawable(logoResId));
        firstTextView.setText(firstTextId);
        secondTextView.setText(secondTextId);
    }
}
