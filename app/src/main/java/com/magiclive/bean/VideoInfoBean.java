package com.magiclive.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.magiclive.AppApplication;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.util.FileUtils;

/**
 * Created by liyanju on 2017/6/3.
 */

public class VideoInfoBean implements Parcelable{

    public String name = "";

    public String path = "";

    public long size;

    public long startTime;

    public long endTime;

    public long duration;

    public boolean isSelection;

    public int volume;

    public long updateTime;

    public int scalingMode = MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;


    public static VideoInfoBean downloadVideoToVideoInfo(DownloadVideo downloadVideo) {
        VideoInfoBean videoInfoBean = new VideoInfoBean();
        videoInfoBean.path = downloadVideo.filePath;
        videoInfoBean.size = downloadVideo.size;
        videoInfoBean.name = downloadVideo.name;
        return videoInfoBean;
    }

    public static VideoInfoBean mediaInfoToVideoInfo(Cursor cursor) {
        VideoInfoBean videoInfo = new VideoInfoBean();
        videoInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        videoInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
        videoInfo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
        videoInfo.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
        if (TextUtils.isEmpty(videoInfo.name)) {
            videoInfo.name = FileUtils.getFileName(videoInfo.path);
        }
        return videoInfo;
    }


    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_DURATION, duration);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_END_TIME, endTime);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_NAME, name);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_PATH, path);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_SELECT, isSelection ? 1 : 0);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_SIZE, size);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_START_TIME, startTime);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_VOLUME, volume);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_TIME, updateTime);
        contentValues.put(MagicLiveContract.VideoContract.VIDEO_SCALINGMODE, scalingMode);
        return contentValues;
    }

    public void cursorToVideoInfoBean(Cursor cursor) {
        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_DURATION));
        endTime = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_END_TIME));
        name = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_NAME));
        path = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_PATH));
        isSelection = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_SELECT)) == 1;
        size = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_SIZE));
        startTime = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_START_TIME));
        volume = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_VOLUME));
        updateTime = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_TIME));
        scalingMode = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_SCALINGMODE));
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_NAME));
    }

    public static boolean isSelection(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_SELECT)) == 1;
    }

    public static long getSize(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_SIZE));
    }

    public static String getPath(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_PATH));
    }

    public static long getStartTime(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_START_TIME));
    }

    public static long getEndTime(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_END_TIME));
    }

    public static long getDuration(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.VideoContract.VIDEO_DURATION));
    }

    public VideoInfoBean() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeLong(this.duration);
        dest.writeByte(this.isSelection ? (byte) 1 : (byte) 0);
        dest.writeInt(this.volume);
        dest.writeLong(this.updateTime);
        dest.writeInt(this.scalingMode);
    }

    protected VideoInfoBean(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.size = in.readLong();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.duration = in.readLong();
        this.isSelection = in.readByte() != 0;
        this.volume = in.readInt();
        this.updateTime = in.readLong();
        this.scalingMode = in.readInt();
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
