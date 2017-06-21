package com.magiclive.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.bean.DownloadVideo;
import com.magiclive.db.DownloadVideoDao;
import com.magiclive.ui.MainActivity;
import com.magiclive.util.FileUtils;
import com.magiclive.util.LogUtils;
import com.magiclive.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by liyanju on 2017/6/20.
 */

public class FileDownloaderHelper {

    public static final String TAG = "FileDownloader";

    private static FileDownloaderHelper fileDownloaderHelper = new FileDownloaderHelper();

    private List<BaseDownloadTask> tasks = new ArrayList<>(5);

    public static Context context;

    private File defaultFile;

    private int downloadId;

    public FileDownloaderHelper() {
        context = AppApplication.getContext();
        defaultFile = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
    }

    public void addDownloadTask(String url, String refererUrl, String detailUrl) {
        if (!defaultFile.exists() || !defaultFile.canWrite() || !defaultFile.canRead()) {
            defaultFile = new File(Environment.getExternalStorageDirectory(), "video_wallpaper");
        }
        FileUtils.createOrExistsDir(defaultFile);
        String path = defaultFile + File.separator + String.valueOf(System.currentTimeMillis())+".mp4";
        LogUtils.v(TAG, "addDownloadTask path " + path + " refererUrl "
                + refererUrl + " detailUrl "+ detailUrl);

        if (downloadId == 0) {
            downloadId = FileDownloader.getImpl().create(url)
                    .setPath(path)
                    .setTag(detailUrl)
                    .addHeader("Referer", refererUrl)
                    .setListener(new NotificationListener(new FileDownloadNotificationHelper<NotificationItem>()))
                    .start();

            DownloadVideo downloadVideo = new DownloadVideo();
            downloadVideo.filePath = path;
            downloadVideo.status = 100;
            downloadVideo.detailUrl = detailUrl;
            DownloadVideoDao.addAndUpdateDownloadVideo(context, downloadVideo);
        }
    }

    public static FileDownloaderHelper getInstances() {
        return fileDownloaderHelper;
    }

    private class NotificationListener extends FileDownloadNotificationListener {

        private static final String TAG = "NotificationListener";

        public NotificationListener(FileDownloadNotificationHelper helper) {
            super(helper);
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(),
                    context.getString(R.string.download_notification_title),
                    context.getString(R.string.download_notification_status));
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
        }

        @Override
        protected void pending(final BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context,
                            DownloadVideo.baseDownloadTaskToDownloadVideo(task));
                }
            }).start();
        }

        @Override
        protected void completed(final BaseDownloadTask task) {
            super.completed(task);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DownloadVideo downloadVideo = DownloadVideo.baseDownloadTaskToDownloadVideo(task);
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, downloadVideo);
                }
            }).start();
        }

        @Override
        public void destroyNotification(BaseDownloadTask task) {
            super.destroyNotification(task);
            downloadId = 0;

            int messageResId = 0;
            switch (task.getStatus()) {
                case FileDownloadStatus.started:
                    messageResId = R.string.start_download;
                    break;
                case FileDownloadStatus.warn:
                case FileDownloadStatus.error:
                    messageResId = R.string.error_download;
                    break;
                case FileDownloadStatus.completed:
                    messageResId = R.string.finish_download;
                    break;
            }

            if (messageResId != 0) {
                ToastUtils.showLongToast(messageResId);
            }
        }
    }

    public static class NotificationItem extends BaseNotificationItem {

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
            Intent intent = new Intent(context, MainActivity.class);

            this.pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());

            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);

        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " prepare";
                    break;
                case FileDownloadStatus.started:
                    desc += " started";
                    break;
                case FileDownloadStatus.progress:
                    desc += " downloading...";
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    break;
            }

            builder.setContentTitle(getTitle())
                    .setContentText(desc);


            if (statusChanged) {
                builder.setTicker(desc);
            }

            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }

    }
}
