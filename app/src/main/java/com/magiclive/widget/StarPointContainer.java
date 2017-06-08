package com.magiclive.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.magiclive.R;
import com.magiclive.util.MyAnimatorListener;
import com.magiclive.util.SizeUtils;


/**
 * Created by liyanju on 2017/4/10.
 */

public class StarPointContainer extends FrameLayout {

    private Paint pointPaint;

    private int width;
    private int height;

    private int length;

    public StarPointContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarPointContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStrokeWidth(SizeUtils.dp2px(5));
        pointPaint.setStrokeCap(Paint.Cap.ROUND);
        pointPaint.setColor(Color.parseColor("#FF6600"));
        pointPaint.setAlpha(pointAlpha);

        angle1 = (float) (point1Radian * Math.PI / 180);
        angle2 = (float) (point2Radian * Math.PI / 180);
        angle3 = (float) (point3Radian * Math.PI / 180);
        angle4 = (float) (point4Radian * Math.PI / 180);
        angle5 = (float) (point5Radian * Math.PI / 180);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        length = Math.max(width, height);
        Log.v("xx", " onSizeChanged width " + width + " height " + height);
    }

    private int pointRadial = 0; // 射线长度

    private int point1Radian = 25; // 第一个点弧度
    private int point2Radian = 90;
    private int point3Radian = 155;
    private int point4Radian = 230;
    private int point5Radian = 310;

    private float angle1;
    private float angle2;
    private float angle3;
    private float angle4;
    private float angle5;

    private boolean isDrawPoint = false;

    private AnimatorSet animatorSet;

    private int pointAlpha = 0;

    private long pointDuration = 150;
    private long starDuration = 100;

    public void startPointSpread(final ImageView starView, final Runnable finishRunnable) {
        ObjectAnimator alphaAnimator = new ObjectAnimator().ofFloat(starView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(starDuration);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(starView, "scaleX", 1.3f, 1f);
        scaleXAnimator.setDuration(starDuration);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(starView, "scaleY", 1.3f, 1f);
        scaleYAnimator.setDuration(starDuration);
        scaleYAnimator.addListener(new MyAnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                starView.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_star));
            }
        });

        ValueAnimator valueAnimator = new ValueAnimator().ofInt(12, 17);
        valueAnimator.setDuration(pointDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                pointRadial = SizeUtils.dp2px(value);
                invalidate();
            }
        });
        valueAnimator.addListener(new MyAnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isDrawPoint = true;
            }
        });
        valueAnimator.setStartDelay(starDuration);

        ValueAnimator valueAnimatorAlphaIn = new ValueAnimator().ofInt(0, 255);
        valueAnimatorAlphaIn.setDuration(pointDuration);
        valueAnimatorAlphaIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                pointAlpha = value;
                invalidate();
            }
        });
        valueAnimatorAlphaIn.setStartDelay(starDuration);

        ValueAnimator valueAnimatorAlphaOut = new ValueAnimator().ofInt(255, 0);
        valueAnimatorAlphaOut.setDuration(50);
        valueAnimatorAlphaOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                pointAlpha = value;
                invalidate();
            }
        });
        valueAnimatorAlphaOut.addListener(new MyAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isDrawPoint = false;
                if (finishRunnable != null) {
                    finishRunnable.run();
                }
            }
        });
        valueAnimatorAlphaOut.setStartDelay(pointDuration + starDuration);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, valueAnimator,
                valueAnimatorAlphaIn, valueAnimatorAlphaOut, alphaAnimator);
        animatorSet.start();
    }

    public void stopPointSpread() {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
            animatorSet = null;
        }
        isDrawPoint = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        if (isDrawPoint) {
            canvas.save();
            pointPaint.setAlpha(pointAlpha);
            canvas.translate(length / 2, length / 2);

            int x1 = (int) (Math.cos(angle1) * (pointRadial * 1.0f));
            int y1 = (int) (Math.sin(angle1) * (pointRadial * 1.0f));
            canvas.drawPoint(x1, y1, pointPaint);


            int x2 = (int) (Math.cos(angle2) * (pointRadial * 1.0f));
            int y2 = (int) (Math.sin(angle2) * (pointRadial * 1.0f));
            canvas.drawPoint(x2, y2, pointPaint);

            int x3 = (int) (Math.cos(angle3) * (pointRadial * 1.0f));
            int y3 = (int) (Math.sin(angle3) * (pointRadial * 1.0f));
            canvas.drawPoint(x3, y3, pointPaint);

            int x4 = (int) (Math.cos(angle4) * (pointRadial * 1.0f));
            int y4 = (int) (Math.sin(angle4) * (pointRadial * 1.0f));
            canvas.drawPoint(x4, y4, pointPaint);

            int x5 = (int) (Math.cos(angle5) * (pointRadial * 1.0f));
            int y5 = (int) (Math.sin(angle5) * (pointRadial * 1.0f));
            canvas.drawPoint(x5, y5, pointPaint);

            canvas.restore();
        }

    }
}
