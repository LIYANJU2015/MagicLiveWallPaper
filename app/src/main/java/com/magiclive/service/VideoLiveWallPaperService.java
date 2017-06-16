package com.magiclive.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import com.devbrackets.android.exomedia.core.exoplayer.ExoMediaPlayer;
import com.devbrackets.android.exomedia.core.listener.ExoPlayerListener;
import com.google.android.exoplayer2.ExoPlayer;
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
    public static final String VIDEO_RELEASE_ACTION = "video_release_action";
    public static final String VIDEO_SCALE_ACTION = "video_scale_action";

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

    public class VideoEngine extends Engine implements Runnable, ExoPlayerListener {

        private ExoMediaPlayer mediaPlayer = null;

        private String path;
        private int start;
        private int end;
        private int volume;
        private int scalingMode;
        private VideoInfoBean curVideoInfoBean;

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
            scalingMode = videoInfoBean.scalingMode;

            curVideoInfoBean = videoInfoBean;
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
            LogUtils.v("registerReceiver start >>");
            IntentFilter intentFilter = new IntentFilter(VIDEO_VOLUME_ACTION);
            intentFilter.addAction(VIDEO_SET_ACTION);
            intentFilter.addAction(VIDEO_RELEASE_ACTION);
            intentFilter.addAction(VIDEO_SCALE_ACTION);
            context.registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null) {
                        return;
                    }
                    LogUtils.v("registerReceiver", "onReceive getAction:: " + intent.getAction());
                    if (VIDEO_VOLUME_ACTION.equals(intent.getAction())) {
                        boolean silence = intent.getBooleanExtra(VIDEO_VOLUME, false);
                        if (mediaPlayer != null) {
                            if (silence) {
                                mediaPlayer.setVolume(0f);
                            } else {
                                float newVolume = volume * 1.f / 100f;
                                mediaPlayer.setVolume(newVolume);
                            }
                        }
                    } else if (VIDEO_SET_ACTION.equals(intent.getAction())) {
                        curVideoInfoBean = intent.getParcelableExtra("VideoInfo");
                        LogUtils.v("registerReceiver", "onReceive newPath " + curVideoInfoBean.path
                                + " path " + path);
                        if (!curVideoInfoBean.path.equals(path)) {
                            initVideoWallPaperParam(curVideoInfoBean);
                            releasePlayer();
                            initMediaPlayer();
                            if (!isVisible()) {
                                mediaPlayer.setPlayWhenReady(false);
                            }
                        } else {
                            initVideoWallPaperParam(curVideoInfoBean);
                            setPlayerConfig();
                        }
                    } else if (VIDEO_RELEASE_ACTION.equals(intent.getAction())) {
                        releasePlayer();
                    } else if (VIDEO_SCALE_ACTION.equals(intent.getAction())) {
                        boolean fitModel = intent.getBooleanExtra("ScalingModeFit", true);
                        if (mediaPlayer != null) {
//                            if (fitModel) {
//                                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                            } else {
//                                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//                            }
                        }
                    }
                }
            }, intentFilter);
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED) {
                mediaPlayer.seekTo(start);
                mediaPlayer.setPlayWhenReady(true);
            }
        }

        @Override
        public void onError(ExoMediaPlayer exoMediaPlayer, Exception e) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onSeekComplete() {

        }

        private void setPlayerConfig() {
            if (mediaPlayer != null) {
                float newVolume = volume * 1.f / 100f;
                LogUtils.v("setVolume", " volume " + volume + " videoVolume " + newVolume);
                mediaPlayer.setVolume(newVolume);
//                mediaPlayer.setVideoScalingMode(scalingMode);
                mediaPlayer.seekTo(start);
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
            LogUtils.v("onVisibilityChangedPlay isVisible " + isVisible()
                    + " mediaPlayer " + mediaPlayer);
            if (isVisible()) {

                if (mediaPlayer == null) {
                    initMediaPlayer();
                    return;
                } else {
                    mediaPlayer.setPlayWhenReady(true);
                }
                startUpdateProgress();
            }  else {
                if (mediaPlayer != null && mediaPlayer.getPlayWhenReady()) {
                    mediaPlayer.setPlayWhenReady(false);
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            LogUtils.v("VideoEngine", "onVisibilityChanged visible " + visible
                     + " path " + path + " isPreview " + isPreview());
            onVisibilityChangedPlay();
        }

        private synchronized void initMediaPlayer() {
            releasePlayer();
            mediaPlayer = new ExoMediaPlayer(context);
            mediaPlayer.setSurface(getSurfaceHolder().getSurface());
            try {
                LogUtils.v("initMediaPlayer", " path " + path);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                mediaPlayer.setUri(Uri.parse(path));
                mediaPlayer.addListener(this);
//                if (curVideoInfoBean != null) {
//                    mediaPlayer.setVideoScalingMode(curVideoInfoBean.scalingMode);
//                }
                mediaPlayer.prepare();
                setPlayerConfig();
                mediaPlayer.setPlayWhenReady(true);
                startUpdateProgress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void startUpdateProgress() {
            if (mediaPlayer == null) {
                return;
            }

            int currentPosition = end - (int)mediaPlayer.getCurrentPosition();
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
            LogUtils.v("VideoEngine", "onSurfaceDestroyed " + hashCode() + " isPreview() " + isPreview());
            releasePlayer();
        }

        public void releasePlayer() {
            if (null != mediaPlayer) {
                UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public static void releaseWallpaperPlayer(Context context) {
        try {
            Intent intent = new Intent(VIDEO_RELEASE_ACTION);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateVideoWallpaper(Context context, VideoInfoBean videoInfoBean) {
        Intent intent = new Intent(VIDEO_SET_ACTION);
        intent.putExtra("VideoInfo", videoInfoBean);
        context.sendBroadcast(intent);
    }

    public static void setScaleVideoWallpaper(Context context, boolean isScalingFit) {
        Intent intent = new Intent(VIDEO_SCALE_ACTION);
        intent.putExtra("ScalingModeFit", isScalingFit);
        context.sendBroadcast(intent);
    }

    public static void setVideoWallpaperVolume(Context context, boolean silence) {
        Intent intent = new Intent(VIDEO_VOLUME_ACTION);
        intent.putExtra(VIDEO_VOLUME, silence);
        context.sendBroadcast(intent);
    }

    public static final int REQUEST_VIDEO_WALLPAPER_CODE = 111;

    public static void startVideoWallpaperPreView(Context context) {
        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                VideoLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, REQUEST_VIDEO_WALLPAPER_CODE);
        } else {
            context.startActivity(intent);
        }
    }
}
