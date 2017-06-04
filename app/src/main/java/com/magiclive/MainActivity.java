package com.magiclive;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.magiclive.bean.VideoInfoBean;
import com.magiclive.service.VideoLiveWallPaperService;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.util.FileUtils;
import com.magiclive.util.LogUtils;



/**
 * Created by liyanju on 2017/6/2.
 */

public class MainActivity extends AppCompatActivity {

    String path = Environment.getExternalStorageDirectory() + "/data/kids/data/x52uv7q.mp4";;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                videoInfoBean.name = FileUtils.getFileName(path);
                // /data/kids/data/x5b0cpy.mp4
                videoInfoBean.path = path;

                VideoWallPaperDetailActivity.launch(MainActivity.this.getApplicationContext(),
                        videoInfoBean);
            }
        });

        RadioGroup radioButton1 = (RadioGroup)findViewById(R.id.RadioGroup);
        radioButton1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.raido1 == checkedId) {
                    path = Environment.getExternalStorageDirectory() + "/data/kids/data/x52uv7q.mp4";
                } else {
                    path = Environment.getExternalStorageDirectory() + "/test1.mp4";
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.v("onActivityResult", "requestCode " + requestCode + " resultCode " + resultCode);
    }
}
