package com.shoutit.app.android.view.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class GalleryActivity extends BaseActivity {

    private static final String KEY_IMAGES_JSON = "key_images_json";
    private static final String KEY_VIDEOS_JSON = "key_videos";
    private static final String KEY_POSITION = "key_position";

    @Bind(R.id.gallery_view)
    ScrollGalleryView galleryView;

    @Inject
    @Named("NoAmazonTransformer")
    Picasso picassoWithoutAmazonTransformer;
    @Inject
    Picasso picasso;
    @Inject
    Gson gson;

    public static Intent newIntent(@Nonnull Context context,
                                   @Nonnull String imagesJson,
                                   @Nonnull String videosJson,
                                   int position) {
        return new Intent(context, GalleryActivity.class)
                .putExtra(KEY_IMAGES_JSON, imagesJson)
                .putExtra(KEY_VIDEOS_JSON, videosJson)
                .putExtra(KEY_POSITION, position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        final Intent intent = checkNotNull(getIntent());
        final String imagesJson = intent.getStringExtra(KEY_IMAGES_JSON);
        final String videosJson = intent.getStringExtra(KEY_VIDEOS_JSON);
        final int position = intent.getIntExtra(KEY_POSITION, 0);

        final List<String> imagesList = gson.fromJson(imagesJson, new TypeToken<List<String>>() {
        }.getType());
        final List<Video> videosList = gson.fromJson(videosJson, new TypeToken<List<Video>>() {
        }.getType());

        galleryView.setThumbnailSize(getResources().getDimensionPixelSize(R.dimen.gallery_thumbnail_size))
                .setZoom(true)
                .setFragmentManager(getSupportFragmentManager());

        for (String imageUrl : imagesList) {
            galleryView.addMedia(MediaInfo.mediaLoader(
                    new MediaLoaders.ImagesLoader(imageUrl, picasso, picassoWithoutAmazonTransformer, getResources())));
        }

        for (Video video : videosList) {
            galleryView.addMedia(MediaInfo.mediaLoader(
                    new MediaLoaders.VideosLoader(picassoWithoutAmazonTransformer, video.getUrl(), video.getThumbnailUrl())));
        }

        galleryView.setCurrentItem(position);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final GalleryActivityComponent component = DaggerGalleryActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
