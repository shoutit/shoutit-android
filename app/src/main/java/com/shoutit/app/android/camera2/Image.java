package com.shoutit.app.android.camera2;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    public long id;
    public String name;
    public String path;
    public boolean isSelected;
    public boolean isVideo;
    public String duration;

    public Image(long id, String name, String path, boolean isSelected,boolean isVideo,String duration) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isSelected = isSelected;
        this.isVideo=isVideo;
        this.duration=duration;
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
        dest.writeByte((byte) (isVideo ? 0x01 : 0x00));
        dest.writeString(duration);

    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in) {
        id = in.readLong();
        name = in.readString();
        path = in.readString();
        isVideo = in.readByte() != 0x00;
        duration=in.readString();

    }
}
