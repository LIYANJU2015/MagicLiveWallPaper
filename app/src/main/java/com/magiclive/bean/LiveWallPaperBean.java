package com.magiclive.bean;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;



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

    public void setLastSDCardVideoInfo(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.VideoColumns.DATA}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                videoInfoBean = new VideoInfoBean();
                videoInfoBean.path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            }
        } finally {
             if (cursor != null) {
                 cursor.close();
             }
        }
    }
}
