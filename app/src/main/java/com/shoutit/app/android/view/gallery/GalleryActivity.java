package com.shoutit.app.android.view.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

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

        final List<String> imagesList = gson.fromJson(imagesJson, new TypeToken<List<String>>() {}.getType());
        final List<Video> videosList = gson.fromJson(videosJson, new TypeToken<List<Video>>() {}.getType());

        galleryView.setThumbnailSize(R.dimen.gallery_thumbnail_size)
                .setZoom(true)
                .setFragmentManager(getSupportFragmentManager());

        for (String imageUrl : imagesList) {
            galleryView.addMedia(MediaInfo.mediaLoader(new ImagesLoader(imageUrl)));
        }

        for (Video video : videosList) {
            galleryView.addMedia(MediaInfo.mediaLoader(new VideosLoader(video.getUrl(), video.getThumbnailUrl())));
        }

        galleryView.setCurrentItem(position);

    }

    class ImagesLoader implements MediaLoader {

        private final String imageUrl;

        public ImagesLoader(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public boolean isImage() {
            return true;
        }

        @Override
        public void loadMedia(Context context, ImageView imageView, SuccessCallback callback) {
            picasso.load(imageUrl)
                    .into(imageView);
        }

        @Override
        public void loadThumbnail(Context context, ImageView thumbnailView, SuccessCallback callback) {
            picasso.load(imageUrl)
                    .resizeDimen(R.dimen.gallery_thumbnail_size, R.dimen.gallery_thumbnail_size)
                    .into(thumbnailView);
        }
    }

    class VideosLoader implements MediaLoader {

        @Nonnull
        private final String videoUrl;
        @Nonnull
        private final String thumbnailUrl;

        public VideosLoader(@Nonnull String videoUrl, @Nonnull String thumbnailUrl) {
            this.videoUrl = videoUrl;
            this.thumbnailUrl = thumbnailUrl;
        }

        @Override
        public boolean isImage() {
            return false;
        }

        @Override
        public void loadMedia(final Context context, ImageView imageView, SuccessCallback callback) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayVideo(context, videoUrl);
                }
            });
        }

        @Override
        public void loadThumbnail(Context context, ImageView thumbnailView, SuccessCallback callback) {
            picasso.load(thumbnailUrl)
                    .resizeDimen(R.dimen.gallery_thumbnail_size, R.dimen.gallery_thumbnail_size)
                    .into(thumbnailView);
        }

        private void displayVideo(Context context, String url) {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra(Constants.URL, url);
            context.startActivity(intent);
        }
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
