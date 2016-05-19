package com.shoutit.app.android.view.chats.chat_media_gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.UniqueItemsScrollGalleryView;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;


public class ChatMediaGalleryActivity extends BaseActivity {

    private static final String EXTRA_CONVERSATION_ID = "conversation_id";

    @Bind(R.id.chat_media_gallery_view)
    UniqueItemsScrollGalleryView galleryView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    ChatMediaGalleryPresenter presenter;

    public static Intent newIntent(Context context, @Nonnull String conversationId) {
        return new Intent(context, ChatMediaGalleryActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_media_gallery);
        ButterKnife.bind(this);

        galleryView.setThumbnailSize(getResources().getDimensionPixelSize(R.dimen.gallery_thumbnail_size))
                .setZoom(true)
                .setFragmentManager(getSupportFragmentManager());

        presenter.getMediaInfoObservable()
                .compose(this.<List<MediaInfo>>bindToLifecycle())
                .subscribe(new Action1<List<MediaInfo>>() {
                    @Override
                    public void call(List<MediaInfo> media) {
                        galleryView.addAll(media);
                    }
                });

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        galleryView.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                boolean shouldLoadMore = position + 3 > galleryView.getViewPager().getAdapter().getCount();
                if (shouldLoadMore) {
                    presenter.getLoadMoreObserver().onNext(null);
                }
            }
        });
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        checkNotNull(conversationId);

        final ChatMediaGalleryActivityComponent component = DaggerChatMediaGalleryActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .chatMediaGalleryActivityModule(new ChatMediaGalleryActivityModule(conversationId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
