package com.magiclive.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.bean.DownloadVideo;
import com.magiclive.bean.OnlineVideoWallPaper;
import com.magiclive.db.DownloadVideoDao;
import com.magiclive.ui.MainActivity;
import com.magiclive.util.BroadcastReceiverUtil;
import com.magiclive.util.FileUtils;
import com.magiclive.util.LogUtils;
import com.magiclive.util.ThreadPoolUtils;
import com.magiclive.util.ToastUtils;

import java.io.File;


/**
 * Created by liyanju on 2017/6/20.
 */

public class FileDownloaderHelper {

    public static final String TAG = "FileDownloader";

    private static FileDownloaderHelper fileDownloaderHelper = new FileDownloaderHelper();

    public static Context context;

    private File defaultFile;

    private int downloadId;

    private ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.FixedThread, 1);

    public static final int TITLE_KEY = 101;
    public static final int DETAIL_KEY = 102;

    public FileDownloaderHelper() {
        context = AppApplication.getContext();
        defaultFile = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
    }

    public void addDownloadTask(String downloadUrl, OnlineVideoWallPaper.OnlineVideo onlineVideo) {
        if (!defaultFile.exists() || !defaultFile.canWrite() || !defaultFile.canRead()) {
            defaultFile = new File(Environment.getExternalStorageDirectory(), "video_wallpaper");
        }
        FileUtils.createOrExistsDir(defaultFile);
        String path = defaultFile + File.separator + String.valueOf(onlineVideo.title) + ".mp4";
        LogUtils.v(TAG, "addDownloadTask path " + path + " downloadUrl "
                + downloadUrl);

        int createDownloadId = FileDownloadUtils.generateId(downloadUrl, path);
        if (DownloadVideoDao.getDownloadVideoById(context, createDownloadId) != null) {
            ToastUtils.showShortToastSafe(R.string.download_added);
            return;
        }

        downloadId = FileDownloader.getImpl().create(downloadUrl)
                .setPath(path)
                .setTag(TITLE_KEY, onlineVideo.title)
                .setTag(DETAIL_KEY, onlineVideo.detailUrl)
                .setListener(new SelfNotificationListener(new FileDownloadNotificationHelper()))
                .start();
        LogUtils.v(TAG, " addDownloadTask downloadId " + downloadId
                + " createDownloadId " + createDownloadId);
        DownloadVideo downloadVideo = new DownloadVideo();
        downloadVideo.url = downloadUrl;
        downloadVideo.id = downloadId;
        downloadVideo.filePath = path;
        downloadVideo.assignDownloadVideo(onlineVideo);

        DownloadVideoDao.addAndUpdateDownloadVideo(context, downloadVideo);

        ToastUtils.showShortToastSafe(R.string.download_add_success);

    }

    public static FileDownloaderHelper getInstances() {
        return fileDownloaderHelper;
    }

    public class SelfNotificationListener extends FileDownloadNotificationListener {

        public SelfNotificationListener(FileDownloadNotificationHelper helper) {
            super(helper);
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(), (String) task.getTag(TITLE_KEY),
                    "");
        }

        @Override
        public void destroyNotification(final BaseDownloadTask task) {
            super.destroyNotification(task);
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    if (task.getStatus() == FileDownloadStatus.completed) {
                        DownloadVideo downloadVideo = DownloadVideo.assignDownloadVideo(task);
                        downloadVideo.isVideoNew = true;
                        DownloadVideoDao.addAndUpdateDownloadVideo(context, downloadVideo);

                        ToastUtils.showShortToast(String.format(context.getString(R.string.download_video_success),
                                task.getTag(TITLE_KEY)));

                        Intent intent = new Intent();
                        intent.putExtra("detail_url", (String)task.getTag(DETAIL_KEY));
                        BroadcastReceiverUtil.sendReceiver(BroadcastReceiverUtil.UPDATE_DOWNLOAD_COUNT, intent);
                    } else {
                        FileUtils.deleteFile(task.getPath());
                        DownloadVideoDao.removeDownloadVideo(context, task.getId());
                    }
                }
            });
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            LogUtils.e("error :: " + e.getMessage());
            ToastUtils.showShortToastSafe(R.string.error_download);
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
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_get_app_black_36dp);

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
                    desc += " downloading... " + (int)(getSofar() * 1f / getTotal() * 1f * 100) + "%";
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

    private class SelfFileDownloadListener extends FileDownloadListener {

        private static final String TAG = "SelfFileDownloadListener";

        @Override
        protected void pending(final BaseDownloadTask task, int soFarBytes, int totalBytes) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
        }

        @Override
        protected void progress(final BaseDownloadTask task, int soFarBytes, int totalBytes) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
        }

        @Override
        protected void completed(final BaseDownloadTask task) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
            LogUtils.v(TAG, "completed getStatus :: " + task.getStatus());
            ToastUtils.showShortToast(String.format(context.getString(R.string.download_video_success),
                    task.getTag(TITLE_KEY)));
        }

        @Override
        protected void paused(final BaseDownloadTask task, int soFarBytes, int totalBytes) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
        }

        @Override
        protected void error(final BaseDownloadTask task, Throwable e) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
            LogUtils.e("error :: " + e.getMessage());
            ToastUtils.showShortToastSafe(R.string.error_download);
        }

        @Override
        protected void warn(final BaseDownloadTask task) {
            threadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadVideoDao.addAndUpdateDownloadVideo(context, DownloadVideo.assignDownloadVideo(task));
                }
            });
        }
    }
}
