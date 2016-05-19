package com.veinhorn.scrollgalleryview;

import com.veinhorn.scrollgalleryview.loader.MediaLoader;

/**
 * Media Info contains the information required to load and display the media in the gallery.
 */
public class MediaInfo {

    private MediaLoader mLoader;
    private String mediaId;

    public static MediaInfo mediaLoader(MediaLoader mediaLoader) {
        return new MediaInfo().setLoader(mediaLoader);
    }

    public MediaLoader getLoader() {
        return mLoader;
    }

    public MediaInfo setLoader(MediaLoader loader) {
        mLoader = loader;
        mediaId = loader.getItemId();
        return this;
    }

    public String getMediaId() {
        return mediaId;
    }
}
