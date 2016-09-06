package com.shoutit.app.android.view.postsignup.interests;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderItem;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class PostSignUpInterestsAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_INTEREST = 2;

    @Inject
    public PostSignUpInterestsAdapter(@ForActivity Context context, LayoutInflater inflater) {
        super(context);
        mInflater = inflater;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.post_signup_header, parent, false));
            case VIEW_TYPE_INTEREST:
                return new CategoryViewHolder(mInflater.inflate(R.layout.post_login_category_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
        holder.bind(items.get(position));
    }

    public class CategoryViewHolder extends ViewHolderManager.BaseViewHolder<PostSignupInterestsPresenter.CategoryItem> {

        @Bind(R.id.post_login_category_text)
        TextView mTextView;
        @Bind(R.id.post_login_category_checkbox)
        CheckBox mCheckBox;
        @Bind(R.id.category_item_container)
        ViewGroup container;

        private CompositeSubscription mCompositeSubscription;

        public CategoryViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PostSignupInterestsPresenter.CategoryItem item) {
            recycle();

            mTextView.setText(item.getName());

            mCompositeSubscription = new CompositeSubscription(RxView.clicks(container)
                    .withLatestFrom(item.selection(), (aVoid, isChecked) -> !isChecked)
                    .subscribe(item.selectionObserver()),

                    item.selection().subscribe(checked -> {
                        mCheckBox.setChecked(checked);
                        container.setBackgroundColor(ContextCompat.getColor(
                                context, checked ? R.color.post_signup_checked_background : R.color.white
                        ));
                    })

            );
        }

        @Override
        public void onViewRecycled() {
            recycle();
            super.onViewRecycled();
        }

        private void recycle() {
            if (mCompositeSubscription != null) {
                mCompositeSubscription.unsubscribe();
                mCompositeSubscription = null;
            }
        }
    }

    public class HeaderViewHolder extends ViewHolderManager.BaseViewHolder<HeaderItem> {
        @Bind(R.id.header_text)
        TextView headerTv;

        public HeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull HeaderItem item) {
            headerTv.setText(item.getHeaderText());
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof PostSignupInterestsPresenter.CategoryItem) {
            return VIEW_TYPE_INTEREST;
        } else if (item instanceof HeaderItem) {
            return VIEW_TYPE_HEADER;
        } else {
            throw new RuntimeException("Unknown view type " + item.getClass().getSimpleName());
        }
    }
}
