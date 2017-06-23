package com.magiclive.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.util.FileUtils;
import com.magiclive.util.LogUtils;

import java.io.File;

import static com.magiclive.R.string.download;

/**
 * Created by liyanju on 2017/6/20.
 */

public class DownloadVideo {

    public int status = -100;

    public String filePath;

    public String detailUrl;

    public String imgUrl;

    public String duration;

    public String title;

    public String url;

    public int id;

    public long totalSize;
    public long completeSize;

    public boolean isVideoNew;

    /**
     * When the task on {@code toLaunchPool} status, it means that the task is just into the
     * LaunchPool and is scheduled for launch.
     * <p>
     * The task is scheduled for launch and it isn't on the FileDownloadService yet.
     */
    public final static byte toLaunchPool = 10;
    /**
     * When the task on {@code toFileDownloadService} status, it means that the task is just post to
     * the FileDownloadService.
     * <p>
     * The task is posting to the FileDownloadService and after this status, this task can start.
     */
    public final static byte toFileDownloadService = 11;

    // by FileDownloadService
    /**
     * When the task on {@code pending} status, it means that the task is in the list on the
     * FileDownloadService and just waiting for start.
     * <p>
     * The task is waiting on the FileDownloadService.
     * <p>
     * The count of downloading simultaneously, you can configure in filedownloader.properties.
     */
    public final static byte PENDING = 1;
    /**
     * When the task on {@code started} status, it means that the network access thread of
     * downloading this task is started.
     * <p>
     * The task is downloading on the FileDownloadService.
     */
    public final static byte STARTED = 6;
    /**
     * When the task on {@code connected} status, it means that the task is successfully connected
     * to the back-end.
     * <p>
     * The task is downloading on the FileDownloadService.
     */
    public final static byte CONNECTED = 2;
    /**
     * When the task on {@code progress} status, it means that the task is fetching data from the
     * back-end.
     * <p>
     * The task is downloading on the FileDownloadService.
     */
    public final static byte PROGRESS = 3;
    /**
     * When the task on {@code blockComplete} status, it means that the task has been completed
     * downloading successfully.
     * <p>
     * The task is completed downloading successfully and the action-flow is blocked for doing
     * something before callback completed method.
     */
    public final static byte BLOCKCOMPLETE = 4;
    /**
     * When the task on {@code retry} status, it means that the task must occur some error, but
     * there is a valid chance to retry, so the task is retry to download again.
     * <p>
     * The task is restarting on the FileDownloadService.
     */
    public final static byte RETRY = 5;

    /**
     * When the task on {@code error} status, it means that the task must occur some error and there
     * isn't any valid chance to retry, so the task is finished with error.
     * <p>
     * The task is finished with an error.
     */
    public final static byte ERROR = -1;
    /**
     * When the task on {@code paused} status, it means that the task is paused manually.
     * <p>
     * The task is finished with the pause action.
     */
    public final static byte PAUSED = -2;
    /**
     * When the task on {@code completed} status, it means that the task is completed downloading
     * successfully.
     * <p>
     * The task is finished with completed downloading successfully.
     */
    public final static byte COMPLETED = -3;
    /**
     * When the task on {@code warn} status, it means that there is another same task(same url,
     * same path to store content) is running.
     * <p>
     * The task is finished with the warn status.
     */
    public final static byte warn = -4;

    /**
     * When the task on {@code INVALID_STATUS} status, it means that the task is IDLE.
     * <p>
     * The task is clear and it isn't launched.
     */
    public final static byte INVALID_STATUS = 0;


    public boolean isAddDownload() {
        return status != 0;
    }

    public boolean isDownloadFinished() {
        return status == FileDownloadStatus.completed;
    }

    public static DownloadVideo assignDownloadVideo(BaseDownloadTask downloadTask) {
        DownloadVideo downloadVideo = new DownloadVideo();
        downloadVideo.filePath = downloadTask.getPath();
        downloadVideo.status = downloadTask.getStatus();
        downloadVideo.totalSize = downloadTask.getSmallFileTotalBytes();
        downloadVideo.id = downloadTask.getId();
        downloadVideo.completeSize = downloadTask.getSmallFileSoFarBytes();

        LogUtils.v("baseDownload to filePath " + downloadVideo.filePath
                + " status " + downloadVideo.status
                + " downloadVideo.detailUrl "
                + downloadVideo.detailUrl
                + " downloadVideo.totalSize "
                + downloadVideo.totalSize
                + " completeSize: " + downloadVideo.completeSize);
        return downloadVideo;
    }

    public void assignDownloadVideo(OnlineVideoWallPaper.OnlineVideo onlineVideo) {
        title = onlineVideo.title;
        duration = onlineVideo.duration;
        detailUrl = onlineVideo.detailUrl;
        imgUrl = onlineVideo.imgUrl;
    }

    public static ContentValues downloadVideoToContentValues(DownloadVideo downloadVideo) {
        ContentValues contentValues = new ContentValues();
        if (!TextUtils.isEmpty(downloadVideo.filePath)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_PATH, downloadVideo.filePath);
        }

        if (downloadVideo.status != -100) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_STATUS, downloadVideo.status);
        }

        if (!TextUtils.isEmpty(downloadVideo.detailUrl)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL, downloadVideo.detailUrl);
        }

        if (!TextUtils.isEmpty(downloadVideo.duration)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_DURATION, downloadVideo.duration);
        }

        if (!TextUtils.isEmpty(downloadVideo.imgUrl)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_IMGURL, downloadVideo.imgUrl);
        }

        if (!TextUtils.isEmpty(downloadVideo.title)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_TITLE, downloadVideo.title);
        }

        if (!TextUtils.isEmpty(downloadVideo.url)) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_URL, downloadVideo.url);
        }

        if (downloadVideo.id != 0) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_ID, downloadVideo.id);
        }

        if (downloadVideo.totalSize != 0) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_TOTALSIZE,
                    downloadVideo.totalSize);
        }

        if (downloadVideo.completeSize != 0) {
            contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_COMPLETESIZE,
                    downloadVideo.completeSize);
        }

        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_NEW,
                downloadVideo.isVideoNew ? 1 : 0);

        return contentValues;
    }

    public static DownloadVideo cursorToDownloadVideo(Cursor cursor) {
        DownloadVideo downloadVideo = new DownloadVideo();
        downloadVideo.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_PATH));
        downloadVideo.status = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_STATUS));
        downloadVideo.detailUrl = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL));
        downloadVideo.duration = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_DURATION));
        downloadVideo.imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_IMGURL));
        downloadVideo.title = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_TITLE));
        downloadVideo.url = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_URL));
        downloadVideo.id = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_ID));
        downloadVideo.completeSize = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_COMPLETESIZE));
        downloadVideo.totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_TOTALSIZE));
        downloadVideo.isVideoNew = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_NEW)) == 1;
        return downloadVideo;
    }
}
