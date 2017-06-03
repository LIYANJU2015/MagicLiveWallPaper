package com.magiclive.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Process;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import com.magiclive.util.LogUtils;
import com.magiclive.util.UIThreadHelper;

import static com.magiclive.AppApplication.getAppPreferences;
import static com.magiclive.WallPaperUtils.createLiveWallpaperIntent;



/**
 * Created by liyanju on 2017/6/2.
 */

public class VideoLiveWallPaperService extends WallpaperService {

    public static final String VIDEO_PATH = "path";
    public static final String VIDEO_VOLUME = "volume";

    private Context context;

    private static final String VIDEO_PARAMS_CONTROL_ACTION = "video_params_control_action";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LogUtils.v("onCreate");
    }

    @Override
    public Engine onCreateEngine() {
        String videoPath = getAppPreferences().getString(VIDEO_PATH, "");
        int start = getAppPreferences().getInt("start", 0);
        int end = getAppPreferences().getInt("end", Integer.MAX_VALUE);
        LogUtils.v("onCreateEngine", " videoPath:: " + videoPath + " start " + start + " end " + end);
        return new VideoEngine(videoPath, start, end);
    }

    public class VideoEngine extends Engine implements Runnable, MediaPlayer.OnCompletionListener {

        private MediaPlayer mediaPlayer = null;

        private String path;

        private int start;
        private int end;

        private BroadcastReceiver mVideoParamsControlReceiver;

        public VideoEngine(String path, int start, int end) {
            this.path = path;
            this.start = start;
            this.end = end;
        }

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return super.getSurfaceHolder();
        }

        @Override
        public void setTouchEventsEnabled(boolean enabled) {
            super.setTouchEventsEnabled(enabled);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            registerReceiver();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            try {
                if (mVideoParamsControlReceiver != null) {
                    unregisterReceiver(mVideoParamsControlReceiver);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private void registerReceiver() {
            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);
            context.registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LogUtils.v("registerReceiver", "onReceive");
                    setPlayerVolume();
                }
            }, intentFilter);
        }

        private void setPlayerVolume() {
            if (mediaPlayer != null) {
                int videoVolume = getAppPreferences().getInt(VIDEO_VOLUME, 0);
                float volume = videoVolume * 1.f / 100f;
                LogUtils.v("setVolume", " volume " + volume + " videoVolume " + videoVolume);
                mediaPlayer.setVolume(volume, volume);
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
            LogUtils.v("VideoEngine", "onSurfaceCreated");
            initMediaPlayer(holder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            LogUtils.v("VideoEngine", "onVisibilityChanged visible " + visible
                    + " myPid " + Process.myPid());
            if (visible) {
                mediaPlayer.start();
                startUpdateProgress();
            } else {
                mediaPlayer.pause();
            }
        }

        private void initMediaPlayer(SurfaceHolder holder) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(holder.getSurface());
            try {
                LogUtils.v("initMediaPlayer", " path " + path);
                mediaPlayer.setDataSource(path);
                mediaPlayer.setOnCompletionListener(this);
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
            LogUtils.v("VideoEngine", "onSurfaceDestroyed " + Process.myPid());
            releasePlayer();
        }

        public void releasePlayer() {
            if (null != mediaPlayer) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public static void setVideoWallpaperVolume(Context context, int volume) {
        getAppPreferences().put(VIDEO_VOLUME, volume);
        Intent intent = new Intent(VIDEO_PARAMS_CONTROL_ACTION);
        context.sendBroadcast(intent);
    }

    public static void startVideoWallpaperPreView(Context context, String videoPath) {
        getAppPreferences().put(VIDEO_PATH, videoPath);

        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                VideoLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 0);
        } else {
            context.startActivity(intent);
        }
    }
}
