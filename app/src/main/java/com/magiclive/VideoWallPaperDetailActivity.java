package com.magiclive;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;


import com.magiclive.bean.VideoInfoBean;
import com.magiclive.util.LogUtils;
import com.magiclive.util.TimeUtils;
import com.magiclive.util.UIThreadHelper;
import com.magiclive.widget.ENPlayView;
import com.magiclive.widget.RangeSeekBar;



/**
 * Created by liyanju on 2017/6/3.
 */

public class VideoWallPaperDetailActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, Runnable, MediaPlayer.OnCompletionListener {

    private RangeSeekBar rangeSeekBar;

    private VideoInfoBean currentVideoInfo;

    private VideoView videoView;

    private float curMin;
    private float curMax;

    private int totalDuration;

    private ENPlayView enPlayView;

    private boolean isSeeking = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_wallpaper_layout);

        currentVideoInfo = getIntent().getParcelableExtra("videoinfo");

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

        rangeSeekBar = (RangeSeekBar)findViewById(R.id.seekbar1);
        rangeSeekBar.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                LogUtils.v("onRangeChanged", " min " + min + " max "
                        + max + " isFromUser " + isFromUser + " curMax " + curMax);
                if (max == 1.0){
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

        videoView = (VideoView)findViewById(R.id.video_view);
        videoView.setVisibility(View.VISIBLE);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setVideoPath(currentVideoInfo.path);
        videoView.setMediaController(null);
        videoView.requestFocus();
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
        LogUtils.v(" startUpdateProgress duration " + duration);
        currentPosition = videoView.getCurrentPosition();
        if (duration >= 0) {
            rangeSeekBar.setValue2(currentPosition);
        } else {
            if (videoView.isPlaying()) {
                videoPause();
            }
        }

        UIThreadHelper.getInstance().getHandler().removeCallbacks(this);
        if (videoView.isPlaying() && !isSeeking) {
            UIThreadHelper.getInstance().getHandler().postDelayed(this, 50);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void run() {
        startUpdateProgress();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtils.v("onPrepared", " getDuration ::" + mp.getDuration());
        curMin = 0;
        curMax = mp.getDuration();
        totalDuration = mp.getDuration();
        rangeSeekBar.setRules(curMin, curMax, 1f, 1);
        rangeSeekBar.setValue(0f, curMax);

        videoStart();

        startUpdateProgress();
    }

    public static void launch(Context context, VideoInfoBean videoInfoBean) {
        Intent intent = new Intent(context, VideoWallPaperDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("videoinfo", videoInfoBean);
        context.startActivity(intent);
    }
}
