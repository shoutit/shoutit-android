package com.shoutit.app.android.view.media;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shoutit.app.android.R;

import butterknife.ButterKnife;
import butterknife.Bind;

public class ImageFragment extends Fragment {

    private static final String EXTRA_IMAGE_URI = ImageFragment.class.getName() + ".image_uri";
    private static final String EXTRA_IMAGE_RESOURCE = ImageFragment.class.getName() + ".image_uri";
    private static final String EXTRA_CAPTION = ImageFragment.class.getName() + ".caption";

    @Bind(R.id.imageview_image)
    ImageView image;

    @Bind(R.id.textview_caption)
    TextView caption;

    private int mImageResource;
    private Uri mImageUri;
    private String mCaption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.init(savedInstanceState);
        } else {
            this.init(getArguments());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this.mImageUri != null) {
            this.image.setImageURI(this.mImageUri);
        } else {
            this.image.setImageResource(this.mImageResource);
        }

        if (this.mCaption != null) {
            this.caption.setText(this.mCaption);
        } else {
            this.caption.setVisibility(View.GONE);
        }
    }

    private void init(Bundle extras) {
        if (extras.containsKey(EXTRA_IMAGE_URI)) {
            this.mImageUri = Uri.parse(extras.getString(EXTRA_IMAGE_URI));
        } else if (extras.containsKey(EXTRA_IMAGE_RESOURCE)) {
            this.mImageResource = extras.getInt(EXTRA_IMAGE_RESOURCE);
        }

        if (extras.containsKey(EXTRA_CAPTION)) {
            this.mCaption = extras.getString(EXTRA_CAPTION);
        }
    }

    public static ImageFragment newInstance(String imageSource, @Nullable String caption) {
        Bundle args = new Bundle();
        args.putString(EXTRA_IMAGE_URI, imageSource);

        if (caption != null) {
            args.putString(EXTRA_CAPTION, caption);
        }

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

}
