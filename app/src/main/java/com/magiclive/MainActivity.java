package com.magiclive;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.magiclive.bean.VideoInfoBean;
import com.magiclive.service.VideoLiveWallPaperService;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.util.LogUtils;

import net.grandcentrix.tray.AppPreferences;

import static android.R.attr.id;


/**
 * Created by liyanju on 2017/6/2.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.video_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                VideoLiveWallPaperService.startVideoWallpaperPreView(MainActivity.this,
                        Environment.getExternalStorageDirectory() + "/test1.mp4");
            }
        });

        findViewById(R.id.video_btn22).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TransparentLiveWallPaperService.startTransparentWallpaperPreView(MainActivity.this);
            }
        });

        findViewById(R.id.video_btn33).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MirrorLiveWallPaperService.startTransparentWallpaperPreView(MainActivity.this);
            }
        });

        findViewById(R.id.btn22).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                VideoInfoBean videoInfoBean = new VideoInfoBean();
                videoInfoBean.name = "test1";
                // /data/kids/data/x5b0cpy.mp4
                videoInfoBean.path = Environment.getExternalStorageDirectory() + "/test1.mp4";
                VideoWallPaperDetailActivity.launch(MainActivity.this.getApplicationContext(),
                        videoInfoBean);
            }
        });

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogUtils.v("onStopTrackingTouch", "getProgress " + seekBar.getProgress());
                VideoLiveWallPaperService.setVideoWallpaperVolume(MainActivity.this, seekBar.getProgress());
            }
        });
    }
}
