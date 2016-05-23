package com.shoutit.app.android.view.media;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {

    private final long id;
    private final String name;
    private final String path;
    private final String duration;

    public Media(long id, String name, String path, String duration) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(duration);

    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    private Media(Parcel in) {
        id = in.readLong();
        name = in.readString();
        path = in.readString();
        duration = in.readString();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDuration() {
        return duration;
    }
}
