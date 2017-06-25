package com.magiclive.bean;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.magiclive.AppApplication;
import com.magiclive.R;

import static android.R.attr.id;


/**
 * Created by liyanju on 2017/6/5.
 */

public class LiveWallPaperBean {

    public static final int VIDEO_LIVE_WALLPAPER = 1;
    public static final int MIRROR_LIVE_WALLPAPER = 2;
    public static final int TRANSPARENT_LIVE_WALLPAPER = 3;

    public int type;

    public String title;

    public VideoInfoBean videoInfoBean;

    public int videoCount;

    public static final String VIDEO_PATH =  "file:///android_asset/test1.mp4";

    public void setLastSDCardVideoInfo(Context context) {
        videoInfoBean = new VideoInfoBean();
        int id = 0;
        try {
            id =R.raw.class.getDeclaredField("test1").getInt(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        videoInfoBean.path = "android.resource://" + AppApplication.getContext().getPackageName() + "/" + id;
        videoInfoBean.name = "Sexy girl";
    }
}
