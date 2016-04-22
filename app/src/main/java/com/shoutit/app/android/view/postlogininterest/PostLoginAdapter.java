package com.shoutit.app.android.view.postlogininterest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class PostLoginAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private final Picasso mPicasso;

    @Inject
    public PostLoginAdapter(@ForActivity Context context, LayoutInflater inflater,
                            @Named("NoAmazonTransformer") Picasso picasso) {
        super(context);
        mInflater = inflater;
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mInflater.inflate(R.layout.post_login_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
        holder.bind(items.get(position));
    }

    public class CategoryViewHolder extends ViewHolderManager.BaseViewHolder<PostLoginPresenter.CategoryItem> {

        @Bind(R.id.post_login_category_text)
        TextView mTextView;

        @Bind(R.id.post_login_category_img)
        ImageView mImageView;

        @Bind(R.id.post_login_category_checkbox)
        CheckBox mCheckBox;

        private CompositeSubscription mCompositeSubscription;

        public CategoryViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PostLoginPresenter.CategoryItem item) {
            recycle();

            mTextView.setText(item.getName());

            mPicasso.load(item.getImageUrl())
                    .into(mImageView);

            mCompositeSubscription = new CompositeSubscription(RxView.clicks(mCheckBox)
                    .withLatestFrom(item.selection(), new Func2<Void, Boolean, Boolean>() {
                        @Override
                        public Boolean call(Void aVoid, Boolean aBoolean) {
                            return !aBoolean;
                        }
                    })
                    .subscribe(item.selectionObserver()),
                    item.selection().subscribe(RxCompoundButton.checked(mCheckBox)));
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
}
