package com.magiclive.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.magiclive.bean.VideoInfoBean;

/**
 * Created by liyanju on 2017/6/4.
 */

public class VideoWallPaperDao {

    public static void removeVideoWallPager(Context context, VideoInfoBean videoInfoBean) {
        String selection = MagicLiveContract.VideoContract.VIDEO_NAME + " = ? and "
                + MagicLiveContract.VideoContract.VIDEO_SIZE + " = ? ";
        String selectionArgs[] = new String[]{videoInfoBean.name, String.valueOf(videoInfoBean.size)};
        context.getContentResolver().delete(MagicLiveContract.VideoContract.CONTENT_URI, selection, selectionArgs);
    }

    public static void setVideoWallPaper(Context context, VideoInfoBean videoInfoBean) {
        Cursor cursor = null;
        try {
            videoInfoBean.updateTime = System.currentTimeMillis();

            String selection = MagicLiveContract.VideoContract.VIDEO_NAME + " = ? and "
                    + MagicLiveContract.VideoContract.VIDEO_SIZE + " = ? ";
            String selectionArgs[] = new String[]{videoInfoBean.name, String.valueOf(videoInfoBean.size)};
            cursor = context.getContentResolver().query(MagicLiveContract.VideoContract.CONTENT_URI, null, selection,
                    selectionArgs, null);
            if (cursor == null) {
                return;
            }

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

    public static void setOtherVideoNoSelect(Context context, VideoInfoBean videoInfoBean) {
        ContentValues contentValues = new ContentValues(1);
        String selection = MagicLiveContract.VideoContract.VIDEO_NAME + " != ? and "
                + MagicLiveContract.VideoContract.VIDEO_SIZE + " != ? ";
        String selectionArgs[] = new String[]{videoInfoBean.name, String.valueOf(videoInfoBean.size)};

        contentValues.put(MagicLiveContract.VideoContract.VIDEO_SELECT, 0);
        context.getContentResolver().update(MagicLiveContract.VideoContract.CONTENT_URI,
                contentValues, selection, selectionArgs);
    }

    public static VideoInfoBean getVideoWallPaper(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MagicLiveContract.VideoContract.CONTENT_URI, null,
                    null, null, MagicLiveContract.VideoContract.VIDEO_TIME + " DESC ");
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
