package com.magiclive.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.VideoWallPaperDao;
import com.magiclive.util.LogUtils;
import com.magiclive.util.UIThreadHelper;

import static com.magiclive.WallPaperUtils.createLiveWallpaperIntent;


/**
 * Created by liyanju on 2017/6/2.
 */

public class VideoLiveWallPaperService extends WallpaperService {

    public static final String VIDEO_PATH = "path";
    public static final String VIDEO_VOLUME = "volume";

    private Context context;

    public static final String VIDEO_VOLUME_ACTION = "video_volume_action";
    public static final String VIDEO_SET_ACTION = "video_set_action";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LogUtils.v("onCreate");
    }

    @Override
    public Engine onCreateEngine() {
        LogUtils.v("onCreateEngine>> " + hashCode());
        return new VideoEngine();
    }

    public class VideoEngine extends Engine implements Runnable, MediaPlayer.OnCompletionListener {

        private MediaPlayer mediaPlayer = null;

        private String path;
        private int start;
        private int end;
        private int volume;

        private BroadcastReceiver mVideoParamsControlReceiver;

        private void initVideoWallPaperParam(VideoInfoBean videoInfoBean) {
            if (videoInfoBean == null) {
                return;
            }
            LogUtils.v("initVideoWallPaperParam", " videoPath:: " + videoInfoBean.path
                    + " start " + videoInfoBean.startTime
                    + " end " + videoInfoBean.endTime
                    + " volume " + videoInfoBean.volume
                    + " path :: " + path);

            path = videoInfoBean.path;
            start = (int)videoInfoBean.startTime;
            end = (int)videoInfoBean.endTime;
            volume = videoInfoBean.volume;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            LogUtils.v("VideoEngine", "onCreate " + hashCode());
            if (!isPreview()) {
                registerReceiver();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            try {
                if (mVideoParamsControlReceiver != null && !isPreview()) {
                    unregisterReceiver(mVideoParamsControlReceiver);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private void registerReceiver() {
            IntentFilter intentFilter = new IntentFilter(VIDEO_VOLUME_ACTION);
            intentFilter.addAction(VIDEO_SET_ACTION);
            context.registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null) {
                        return;
                    }
                    LogUtils.v("registerReceiver", "onReceive");
                    if (VIDEO_VOLUME_ACTION.equals(intent.getAction())) {
                        setPlayerVolume();
                    } else if (VIDEO_SET_ACTION.equals(intent.getAction())) {
                        VideoInfoBean videoInfoBean = intent.getParcelableExtra("VideoInfo");
                        LogUtils.v("registerReceiver", "onReceive newPath " + videoInfoBean.path + " path " + path);
                        if (!videoInfoBean.path.equals(path)) {
                            initVideoWallPaperParam(videoInfoBean);
                            releasePlayer();
                            initMediaPlayer();
                            if (!isVisible()) {
                                mediaPlayer.pause();
                            }
                        }
                    }
                }
            }, intentFilter);
        }

        private void setPlayerVolume() {
            if (mediaPlayer != null) {
                float newVolume = volume * 1.f / 100f;
                LogUtils.v("setVolume", " volume " + volume + " videoVolume " + newVolume);
                mediaPlayer.setVolume(newVolume, newVolume);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            LogUtils.v("VideoEngine", "onSurfaceChanged");
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            initVideoWallPaperParam(VideoWallPaperDao.getVideoWallPaper(context));
            LogUtils.v("VideoEngine", "onSurfaceCreated " + hashCode());

            initMediaPlayer();
        }

        private void onVisibilityChangedPlay() {
            if (isVisible()) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                startUpdateProgress();
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            LogUtils.v("VideoEngine", "onVisibilityChanged visible " + visible
                    + " hashCode " + hashCode() + " path " + path + " isPreview " + isPreview());
            onVisibilityChangedPlay();
        }

        private synchronized void initMediaPlayer() {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setSurface(getSurfaceHolder().getSurface());
            }
            try {
                LogUtils.v("initMediaPlayer", " path " + path);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                setPlayerVolume();
                mediaPlayer.prepare();
                mediaPlayer.seekTo(start);
                mediaPlayer.start();
                startUpdateProgress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtils.v("VideoEngine", "onCompletion >>");
            mediaPlayer.seekTo(start);
            mediaPlayer.start();
        }

        private void startUpdateProgress() {
            if (mediaPlayer == null) {
                return;
            }

            int currentPosition = end - mediaPlayer.getCurrentPosition();
            if (currentPosition <= 0) {
                LogUtils.v("VideoEngine", "startUpdateProgress seekto >>");
                mediaPlayer.seekTo(this.start);
            }
            UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
            if (isVisible()) {
                UIThreadHelper.getInstance().getHandler().postDelayed(this, currentPosition <= 100 ? (long) currentPosition : (long) (currentPosition / 2));
            }
        }

        @Override
        public void run() {
            startUpdateProgress();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            LogUtils.v("VideoEngine", "onSurfaceDestroyed " + hashCode());
            releasePlayer();
        }

        public void releasePlayer() {
            UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
            if (null != mediaPlayer) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public static void setVideoWallpaperVolume(Context context, int volume) {
        Intent intent = new Intent(VIDEO_VOLUME_ACTION);
        intent.putExtra(VIDEO_VOLUME, volume);
        context.sendBroadcast(intent);
    }

    public static void setVideoWallpaper(Context context, VideoInfoBean videoInfoBean) {
        Intent intent = new Intent(VIDEO_SET_ACTION);
        intent.putExtra("VideoInfo", videoInfoBean);
        context.sendBroadcast(intent);
    }

    public static void startVideoWallpaperPreView(Context context) {
        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                VideoLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 0);
        } else {
            context.startActivity(intent);
        }
    }
}
