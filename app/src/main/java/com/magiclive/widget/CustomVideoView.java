package com.magiclive.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.magiclive.util.LogUtils;

/**
 * Created by liyanju on 2017/6/3.
 */

public class CustomVideoView extends VideoView{

    private int videowidth;
    private int videoheight;

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoSize(int videowidth, int videoheight) {
        this.videowidth = videowidth;
        this.videoheight = videoheight;
        requestLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtils.v("onMeasure " + videowidth + " videoheight " + videoheight);
        int defaultwidth = getDefaultSize(this.videowidth, widthMeasureSpec);
        int defaultheight = getDefaultSize(this.videoheight, heightMeasureSpec);
        if (this.videowidth > 0 && this.videoheight > 0) {
            if (this.videowidth * defaultheight > this.videoheight * defaultwidth) {
                defaultheight = (this.videoheight * defaultwidth) / this.videowidth;
            } else if (this.videowidth * defaultheight < this.videoheight * defaultwidth) {
                defaultwidth = (this.videowidth * defaultheight) / this.videoheight;
            }
        }
        LogUtils.v("onMeasure22 " + defaultwidth + " defaultheight " + defaultheight);
        setMeasuredDimension(defaultwidth, defaultheight);
    }
}
