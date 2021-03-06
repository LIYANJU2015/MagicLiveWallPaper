package com.magiclive.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.ads.AdView;
import com.magiclive.AdViewManager;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.VideoWallPaperDao;
import com.magiclive.service.VideoLiveWallPaperService;
import com.magiclive.util.DeviceUtils;
import com.magiclive.util.LogUtils;
import com.magiclive.util.SizeUtils;
import com.magiclive.util.StatusBarColorCompat;
import com.magiclive.util.TimeUtils;
import com.magiclive.util.ToastUtils;
import com.magiclive.util.UIThreadHelper;
import com.magiclive.widget.ENPlayView;
import com.magiclive.widget.RangeSeekBar;


/**
 * Created by liyanju on 2017/6/3.
 */

public class VideoWallPaperDetailActivity extends Activity implements MediaPlayer.OnPreparedListener, Runnable, MediaPlayer.OnCompletionListener {

    private RangeSeekBar rangeSeekBar;

    private VideoInfoBean curVideoInfo;

    private VideoView videoView;

    private float curMin;
    private float curMax;
    private float curVolume;

    private int totalDuration;

    private ENPlayView enPlayView;

    private boolean isSeeking = false;

    private Button setBtn;

    private Context context;

    private TextView volumeTV;

    private LinearLayout topLinear;
    private LinearLayout bottomLinear;

    private void loadVideoInfo(VideoInfoBean videoInfoBean) {
        if (videoInfoBean == null) {
            return;
        }

        if (!AppApplication.getSPUtils().getBoolean("audio", true)) {
            videoInfoBean.volume = 0;
        }

        if (AppApplication.getSPUtils().getBoolean("scale", true)) {
            videoInfoBean.scalingMode = MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
        } else {
            videoInfoBean.scalingMode = MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
        }


        curVolume = videoInfoBean.volume * 1.f / 100f;
        curMin = videoInfoBean.startTime;
        curMax = videoInfoBean.endTime;
    }

    private Rect topRect = new Rect();
    private Rect bottomRect = new Rect();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_wallpaper_layout);
        context = getApplicationContext();
        LogUtils.v("onCreate");

        curVideoInfo = getIntent().getParcelableExtra("videoinfo");
        loadVideoInfo(curVideoInfo);

        initSetTextView();

        initRandSeekBarView();

        initVolumeSeekBar();

        initRandSeekBarView();

        initVideoView();

        initAdView();
    }

    private void initAdView() {
        AdView adView = (AdView)findViewById(R.id.banner_view);
        AdViewManager.getInstances().addCurrBannerAdView(VideoWallPaperDetailActivity.class, adView);
    }

    private void initSetTextView() {
        bottomLinear = (LinearLayout)findViewById(R.id.video_wallpaper_bottom);
        volumeTV = (TextView)findViewById(R.id.volume_tv);
        volumeTV.setText(String.format(getString(R.string.volume_text),
                String.valueOf(curVideoInfo.volume)));

        setBtn = (Button)findViewById(R.id.set_btn);
        setBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                curVideoInfo.startTime = (long)curMin;
                curVideoInfo.endTime = (long)curMax;
                if (curVideoInfo.isSelection) {
                    VideoLiveWallPaperService.updateVideoWallpaper(context, curVideoInfo);
                    ToastUtils.showShortToast(getString(R.string.apply_success_text));
                    VideoWallPaperDao.setVideoWallPaper(context, curVideoInfo);
                    ((AppApplication)getApplication()).getAppManager().killAll();
                } else {
                    curVideoInfo.isSelection = true;
                    VideoWallPaperDao.setVideoWallPaper(context, curVideoInfo);
                    VideoLiveWallPaperService.startVideoWallpaperPreView(VideoWallPaperDetailActivity.this);
                    // 同时只能有一个播放器存在，看以后换播放器能否解决
                    videoView.stopPlayback();
                }
            }
        });

        if (curVideoInfo.isSelection) {
            setBtn.setText(getString(R.string.apply_text));
        } else {
            setBtn.setText(getString(R.string.set_video_wallpaper));
        }

        TextView titleTV = (TextView)findViewById(R.id.title);
        titleTV.setText(curVideoInfo.name);
        topLinear = (LinearLayout)findViewById(R.id.video_wallpaper_top);
        topLinear.setPadding(SizeUtils.dp2px(35),
                StatusBarColorCompat.getStatusBarHeight(context),
                SizeUtils.dp2px(35), SizeUtils.dp2px(20));

        findViewById(R.id.contentFrame).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                topRect.setEmpty();
                bottomRect.setEmpty();

                topLinear.getHitRect(topRect);
                bottomLinear.getHitRect(bottomRect);

                if (topLinear.getVisibility() == View.GONE) {
                    topLinear.setVisibility(View.VISIBLE);
                    bottomLinear.setVisibility(View.VISIBLE);
                } else if (!topRect.contains((int)event.getX(), (int)event.getY())
                        && !bottomRect.contains((int)event.getX(), (int)event.getY())) {
                    topLinear.setVisibility(View.GONE);
                    bottomLinear.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }

    private void initVideoView() {
        enPlayView = (ENPlayView)findViewById(R.id.play_view);
        enPlayView.play();
        enPlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.v("onClick isPlaying:: " + videoView.isPlaying());
                if (videoView.isPlaying()) {
                    videoPause();
                } else {
                    videoStart();
                    startUpdateProgress();
                }
            }
        });

        videoView = (VideoView)findViewById(R.id.video_view);
        videoView.setVisibility(View.VISIBLE);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setVideoPath(curVideoInfo.path);
        videoView.setMediaController(null);
        videoView.requestFocus();
    }

    private void initVolumeSeekBar() {
        RangeSeekBar volumeSeekbar = (RangeSeekBar)findViewById(R.id.volume_seekbar);
        volumeSeekbar.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                LogUtils.v("volumeSeekbar min " + min + " isFromUser " + isFromUser);
                if (!isFromUser) {
                    curVolume = min;
                    curVideoInfo.volume = (int)curVolume;
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(curVideoInfo.volume * 1.f / 100f, curVideoInfo.volume * 1.f / 100f);
                    }
                }
                volumeTV.setText(String.format(getString(R.string.volume_text),
                        String.valueOf(curVideoInfo.volume)));
            }
        });
        volumeSeekbar.setValue(curVideoInfo.volume);
    }

    private TextView starTimeTV;
    private TextView endTimeTV;
    private TextView runTimeTV;

    private void initRandSeekBarView() {
        rangeSeekBar = (RangeSeekBar)findViewById(R.id.range_seekbar);
        rangeSeekBar.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                LogUtils.v("onRangeChanged", " min " + min + " max "
                        + max + " isFromUser " + isFromUser + " curMax " + curMax);
                if (max <= 1.0){
                    return;
                }

                isSeeking = true;
                rangeSeekBar.resetProgress();

                if (curMax != max) {
                    videoView.seekTo((int) max);
                    if (!isFromUser) {
                        isSeeking = false;
                        curMax = max;
                        LogUtils.v("onRangeChanged", ">>> pause");
                        videoPause();
                        rangeSeekBar.resetProgress();
                    }
                    view.setProgressDescription(TimeUtils.stringForTime((int)max));
                } else {
                    if (curMin != min) {
                        videoView.seekTo((int)min);
                        if (!isFromUser) {
                            curMin = min;
                            LogUtils.v("onRangeChanged", ">>> start");
                            isSeeking = false;
                            videoStart();
                            startUpdateProgress();
                        }
                    }
                    view.setProgressDescription(TimeUtils.stringForTime((int)min));
                }
            }
        });

        starTimeTV = (TextView)findViewById(R.id.start_time);
        endTimeTV = (TextView)findViewById(R.id.end_time);
        runTimeTV = (TextView)findViewById(R.id.run_time);
        runTimeTV.setText(TimeUtils.stringForTime(0));
    }

    private void setLiveWallPaperSuccess() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoWallPaperDao.setOtherVideoNoSelect(context, curVideoInfo);
                VideoWallPaperDao.setVideoWallPaper(context, curVideoInfo);
            }
        }).start();

        VideoLiveWallPaperService.updateVideoWallpaper(context, curVideoInfo);
        ((AppApplication)getApplication()).getAppManager().killAll();

        if (AppApplication.isShowRatingDialog()) {
            AppApplication.setShowRatingDialog();
            RatingActivity.launch(context);
        }
    }

    private void cancelSetLiveWallPaper() {
        curVideoInfo.isSelection = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoWallPaperDao.removeVideoWallPager(context, curVideoInfo);
            }
        }).start();
        // 同时只能有一个播放器存在，看以后换播放器能否解决
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.v("onActivityResult", " requestCode " + requestCode + " resultCode " + resultCode);
        if (DeviceUtils.getSDKVersion() >= Build.VERSION_CODES.LOLLIPOP) {
            if (resultCode == Activity.RESULT_OK) {
                setLiveWallPaperSuccess();
            } else {
                cancelSetLiveWallPaper();
            }
        } else {
            setLiveWallPaperSuccess();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        enPlayView.pause();
    }

    private void videoStart() {
        if (!videoView.isPlaying()) {
            enPlayView.play();
            if (currentPosition + 100 >= curMax) {
                videoView.seekTo((int) curMin);
            } else {
                videoView.seekTo(currentPosition);
            }
            videoView.start();
        }
    }

    private void videoPause() {
        enPlayView.pause();
        videoView.pause();
    }

    private int currentPosition = 0;

    private void startUpdateProgress() {
        int duration = (int)curMax - videoView.getCurrentPosition();
        LogUtils.v(" startUpdateProgress duration " + duration
                + " getCurrentPosition : " + videoView.getCurrentPosition() + " curMax " + curMax);
        currentPosition = videoView.getCurrentPosition();
        if (duration >= 0) {
            rangeSeekBar.setValue2(currentPosition);
        } else {
            if (videoView.isPlaying()) {
                videoPause();
            }
        }

        runTimeTV.setText(TimeUtils.stringForTime(Math.abs(videoView.getCurrentPosition() - (int)curMin)));

        UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
        if (videoView.isPlaying() && !isSeeking) {
            UIThreadHelper.getInstance().getHandler().postDelayed(this, 50);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
    }

    @Override
    public void run() {
        startUpdateProgress();
    }

    private MediaPlayer mediaPlayer;

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtils.v("onPrepared", " getDuration ::" + mp.getDuration()
                + " curMin " + curMin + " curMax " + curMax);
        if (mp.getDuration() == 0) {
            return;
        }

        mediaPlayer = mp;
        if (curMax == 0) {
            curMax = mp.getDuration();
        }
        totalDuration = mp.getDuration();

        rangeSeekBar.setRules(0, mp.getDuration(), 1f, 1);
        rangeSeekBar.setValue(curMin, curMax);

        mediaPlayer.setVolume(curVolume, curVolume);
        mediaPlayer.setVideoScalingMode(curVideoInfo.scalingMode);

        currentPosition = (int)curMin;

        videoStart();

        startUpdateProgress();

        starTimeTV.setText(TimeUtils.stringForTime(0));
        endTimeTV.setText(TimeUtils.stringForTime(totalDuration));
    }

    public static void launch(Context context, VideoInfoBean videoInfoBean) {
        VideoLiveWallPaperService.releaseWallpaperPlayer(context);
        Intent intent = new Intent(context, VideoWallPaperDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("videoinfo", videoInfoBean);
        context.startActivity(intent);
    }
}
