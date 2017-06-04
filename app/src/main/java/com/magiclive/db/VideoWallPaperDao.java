package com.magiclive.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.magiclive.bean.VideoInfoBean;

/**
 * Created by liyanju on 2017/6/4.
 */

public class VideoWallPaperDao {

    public static void setVideoWallPaper(Context context, VideoInfoBean videoInfoBean) {
        Cursor cursor = null;
        try {
            String selection = MagicLiveContract.VideoContract.VIDEO_NAME + " = ? and "
                    + MagicLiveContract.VideoContract.VIDEO_SIZE + " = ? ";
            String selectionArgs[] = new String[]{videoInfoBean.name, String.valueOf(videoInfoBean.size)};
            cursor = context.getContentResolver().query(MagicLiveContract.VideoContract.CONTENT_URI, null, selection,
                    selectionArgs, null);
            if (cursor == null) {
                return;
            }

            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MagicLiveContract.VideoContract.VIDEO_SELECT, 0);
            context.getContentResolver().update(MagicLiveContract.VideoContract.CONTENT_URI,
                    contentValues, MagicLiveContract.VideoContract.VIDEO_SELECT + " = 1", null);

            if ( cursor.getCount() > 0) {
                context.getContentResolver().update(MagicLiveContract.VideoContract.CONTENT_URI,
                        videoInfoBean.toContentValues(), selection, selectionArgs);
            } else {
                context.getContentResolver().insert(MagicLiveContract.VideoContract.CONTENT_URI, videoInfoBean.toContentValues());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static VideoInfoBean getVideoWallPaper(Context context) {
        Cursor cursor = null;
        try {
            String selection = MagicLiveContract.VideoContract.VIDEO_SELECT + " = 1";
            cursor = context.getContentResolver().query(MagicLiveContract.VideoContract.CONTENT_URI, null,
                    selection, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                VideoInfoBean videoInfoBean = new VideoInfoBean();
                videoInfoBean.cursorToVideoInfoBean(cursor);
                return videoInfoBean;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
