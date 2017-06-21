package com.magiclive.bean;

import android.content.ContentValues;
import android.database.Cursor;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.util.FileUtils;
import com.magiclive.util.LogUtils;

import java.io.File;

/**
 * Created by liyanju on 2017/6/20.
 */

public class DownloadVideo {

    public int status;

    public String name;

    public String filePath;

    public long size;

    public String detailUrl;

    public String imgUrl;

    public String duration;

    public String title;

    public boolean isAddDownload() {
        return status != 0;
    }

    public boolean isDownloadFinished() {
        return status == FileDownloadStatus.completed;
    }

    public static DownloadVideo baseDownloadTaskToDownloadVideo(BaseDownloadTask downloadTask) {
        DownloadVideo downloadVideo = new DownloadVideo();
        downloadVideo.name = downloadTask.getFilename();
        downloadVideo.filePath = downloadTask.getPath();
        downloadVideo.status = downloadTask.getStatus();
        downloadVideo.detailUrl = (String)downloadTask.getTag();
        LogUtils.v("baseDownload to filePath " + downloadVideo.filePath
                + " status " + downloadVideo.status
                + " downloadVideo.detailUrl " + downloadVideo.detailUrl);
        return downloadVideo;
    }

    public static ContentValues downloadVideoToContentValues(DownloadVideo downloadVideo) {
        ContentValues contentValues = new ContentValues();
        File file = new File(downloadVideo.filePath);
        downloadVideo.size = file.length();
        downloadVideo.name = FileUtils.getFileName(downloadVideo.filePath);

        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_NAME, downloadVideo.name);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_PATH, downloadVideo.filePath);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_SIZE, downloadVideo.size);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_STATUS, downloadVideo.status);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL, downloadVideo.detailUrl);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_DURATION, downloadVideo.duration);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_IMGURL, downloadVideo.imgUrl);
        contentValues.put(MagicLiveContract.DownloadVideoContract.VIDEO_TITLE, downloadVideo.title);
        return contentValues;
    }

    public static DownloadVideo cursorToDownloadVideo(Cursor cursor) {
        DownloadVideo downloadVideo = new DownloadVideo();
        downloadVideo.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_PATH));
        downloadVideo.name = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_NAME));
        downloadVideo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_SIZE));
        downloadVideo.status = cursor.getInt(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_STATUS));
        downloadVideo.detailUrl = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_DETAL_URL));
        downloadVideo.duration = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_DURATION));
        downloadVideo.imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_IMGURL));
        downloadVideo.title = cursor.getString(cursor.getColumnIndexOrThrow(MagicLiveContract.DownloadVideoContract.VIDEO_TITLE));
        return downloadVideo;
    }
}
