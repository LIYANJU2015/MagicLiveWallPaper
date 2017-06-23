package com.magiclive.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.magiclive.bean.DownloadVideo;
import com.magiclive.util.LogUtils;

import java.util.ArrayList;

import static android.R.attr.id;
import static android.R.attr.path;
import static com.magiclive.download.FileDownloaderHelper.context;

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

    public static void removeDownloadVideo(Context context, int id) {
        String where = MagicLiveContract.DownloadVideoContract.VIDEO_ID + " = ? ";
        String selectionArgs[] = new String[]{String.valueOf(id)};
        context.getContentResolver().delete(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                where, selectionArgs);
    }

    public static void addAndUpdateDownloadVideo(Context context, DownloadVideo downloadVideo) {
        if (getDownloadVideoById(context, downloadVideo.id) != null) {
            LogUtils.v("addAndUpdateDownloadVideo has downloadvideo");
            String selection = MagicLiveContract.DownloadVideoContract.VIDEO_PATH + " = ? ";
            String selectionArgs[] = new String[]{downloadVideo.filePath};
            context.getContentResolver().update(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                    DownloadVideo.downloadVideoToContentValues(downloadVideo), selection, selectionArgs);
            return;
        }
        LogUtils.v("addAndUpdateDownloadVideo insert");
        context.getContentResolver().insert(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                DownloadVideo.downloadVideoToContentValues(downloadVideo));
    }

    public static int getDownloadNewCount() {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_NEW
                + " = ? ";
        String selectionArgs[] = new String[]{String.valueOf(1)};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, selection, selectionArgs, null);
            if (cursor != null) {
                return cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    public static void updateDownloadVideoNew(Context context, int id) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_ID
                + " = ? ";
        String selectionArgs[] = new String[]{String.valueOf(id)};
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_NEW, false);
        context.getContentResolver()
                .update(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                        contentValues, selection, selectionArgs);
    }

    public static DownloadVideo getDownloadVideoById(Context context, int id) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_ID
                + " = ? ";
        String selectionArgs[] = new String[]{String.valueOf(id)};
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

    public static int getDownloadStatusById(Context context, String detailUrl) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL
                + " = ? ";
        String selectionArgs[] = new String[]{String.valueOf(detailUrl)};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -100;
    }

    public static boolean isHasDownloading(Context context) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_STATUS + " != ?";
        String selectionArgs[] = new String[]{String.valueOf(DownloadVideo.COMPLETED)};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static ArrayList<DownloadVideo> getAllDownloadVideo(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(MagicLiveContract.DownloadVideoContract.CONTENT_URI,
                            null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                ArrayList<DownloadVideo> list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    DownloadVideo downloadVideo = DownloadVideo.cursorToDownloadVideo(cursor);
                    list.add(downloadVideo);
                }
                return list;
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
