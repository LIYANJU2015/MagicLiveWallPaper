package com.magiclive.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyanju on 2017/6/3.
 */

public class VideoInfoBean implements Parcelable{

    public String name;

    public String path;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
    }

    public VideoInfoBean() {
    }

    protected VideoInfoBean(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
    }

    public static final Creator<VideoInfoBean> CREATOR = new Creator<VideoInfoBean>() {
        @Override
        public VideoInfoBean createFromParcel(Parcel source) {
            return new VideoInfoBean(source);
        }

        @Override
        public VideoInfoBean[] newArray(int size) {
            return new VideoInfoBean[size];
        }
    };
}
