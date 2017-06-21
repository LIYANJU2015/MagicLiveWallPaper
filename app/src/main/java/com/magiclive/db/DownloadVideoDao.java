package com.magiclive.db;

import android.content.Context;
import android.database.Cursor;

import com.magiclive.bean.DownloadVideo;

/**
 * Created by liyanju on 2017/6/20.
 */

public class DownloadVideoDao {

    public static void removeDownloadVideo(Context context, String path) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_PATH + " = ? ";
        String selectionArgs[] = new String[]{path};
        context.getContentResolver().delete(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                selection, selectionArgs);
    }

    public static void addAndUpdateDownloadVideo(Context context, DownloadVideo downloadVideo) {
        if (getDownloadVideoByPath(context, downloadVideo.filePath) != null) {
            String selection = MagicLiveContract.DownloadVideoContract.VIDEO_PATH + " = ? ";
            String selectionArgs[] = new String[]{downloadVideo.filePath};
            context.getContentResolver().update(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                    DownloadVideo.downloadVideoToContentValues(downloadVideo), selection, selectionArgs);
            return;
        }
        context.getContentResolver().insert(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                DownloadVideo.downloadVideoToContentValues(downloadVideo));
    }

    public static DownloadVideo getDownloadVideoByPath(Context context, String path) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_PATH + " = ? ";
        String selectionArgs[] = new String[]{path};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return DownloadVideo.cursorToDownloadVideo(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static DownloadVideo getDownloadVideoByDetailUrl(Context context, String detailUrl) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL + " = ? ";
        String selectionArgs[] = new String[]{detailUrl};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return DownloadVideo.cursorToDownloadVideo(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
