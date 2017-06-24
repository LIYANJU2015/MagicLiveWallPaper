package com.magiclive.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.util.MyAnimatorListener;
import com.magiclive.widget.StarPointContainer;


public class RatingActivity extends Activity {

    protected int activityCloseEnterAnimation;

    protected int activityCloseExitAnimation;

    private boolean mFinishWithAnimation = true;

    private ImageView startIVS[] = new ImageView[5];
    private StarPointContainer starFrameLayouts[] = new StarPointContainer[5];

    private AnimatorSet animatorSet;

    public static void launch(Context context) {
        try {
            Intent intent = new Intent(context, RatingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle extras) {
        super.onCreate(extras);
        setContentView(R.layout.activity_rating);

        activateAnimation();

        initView();
    }

    private void initView() {
        TextView titleTV = (TextView)findViewById(R.id.title_tv);
        titleTV.setText(String.format(getString(R.string.rating_title), getString(R.string.app_name)));

        final ImageView starIV1 = (ImageView) findViewById(R.id.star_iv1);
        final StarPointContainer starFrameLayout1 = (StarPointContainer) findViewById(R.id.star1);
        startIVS[0] = starIV1;
        starFrameLayouts[0] = starFrameLayout1;

        final ImageView starIV2 = (ImageView) findViewById(R.id.star_iv2);
        final StarPointContainer starFrameLayout2 = (StarPointContainer) findViewById(R.id.star2);
        startIVS[1] = starIV2;
        starFrameLayouts[1] = starFrameLayout2;

        final ImageView starIV3 = (ImageView) findViewById(R.id.star_iv3);
        final StarPointContainer starFrameLayout3 = (StarPointContainer) findViewById(R.id.star3);
        startIVS[2] = starIV3;
        starFrameLayouts[2] = starFrameLayout3;

        final ImageView starIV4 = (ImageView) findViewById(R.id.star_iv4);
        final StarPointContainer starFrameLayout4 = (StarPointContainer) findViewById(R.id.star4);
        startIVS[3] = starIV4;
        starFrameLayouts[3] = starFrameLayout4;

        final ImageView starIV5 = (ImageView) findViewById(R.id.star_iv5);
        final StarPointContainer starFrameLayout5 = (StarPointContainer) findViewById(R.id.star5);
        startIVS[4] = starIV5;
        starFrameLayouts[4] = starFrameLayout5;

        final ImageView handleIV = (ImageView)findViewById(R.id.handle_icon_iv);

        starIV1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > 16) {
                    starIV1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    starIV1.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                starIV1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleMoveAnimation(handleIV, starIV5, new Runnable() {
                            @Override
                            public void run() {
                                startAnimationSpread();
                            }
                        });
                    }
                }, 600);
            }
        });
    }


    private void handleMoveAnimation(ImageView handleIV, View targetView, final Runnable endRunnable) {
        int[] location = new int[2];
        targetView.getLocationInWindow(location);

        ObjectAnimator XAnimator = ObjectAnimator.ofFloat(handleIV, "x", handleIV.getX(), location[0]);
        XAnimator.setDuration(200);
        ObjectAnimator YAnimator = ObjectAnimator.ofFloat(handleIV, "y", handleIV.getY(), location[1]);
        YAnimator.setDuration(200);
        ObjectAnimator aplhaAnimator = ObjectAnimator.ofFloat(handleIV, "alpha", 1f, 0f);
        aplhaAnimator.setDuration(200);
        aplhaAnimator.setStartDelay(200);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(XAnimator, YAnimator, aplhaAnimator);
        animatorSet.addListener(new MyAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        });
        animatorSet.start();
    }

    private int currentIndex = 0;

    private void startAnimationSpread() {
        starFrameLayouts[currentIndex].startPointSpread(startIVS[currentIndex], new Runnable() {
            @Override
            public void run() {
                currentIndex++;
                if (currentIndex < 5) {
                    starFrameLayouts[currentIndex].startPointSpread(startIVS[currentIndex], this);
                } else {
                    currentIndex = 0;
                }
            }
        });
    }

    private void stopAnimationSpread() {
        for (StarPointContainer starPointContainer : starFrameLayouts) {
            starPointContainer.stopPointSpread();
        }

        for (ImageView starImg : startIVS) {
            starImg.setImageResource(R.drawable.ic_rating_star);
        }
    }

    /**
     * 此方法可以使 activity 退出动画生效
     */
    private void activateAnimation() {
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();

        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId,
                new int[]{android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
        activityStyle.recycle();
    }


    public void onRatingButton1to4Clicked(View v) {
        mFinishWithAnimation = false;
        finish();
    }

    public void onRatingButtonClicked(View v) {
        mFinishWithAnimation = false;
        finish();
        gotoGP();
        AppApplication.getSPUtils().put("ratingcount", 5);
    }

    @Override
    public void finish() {
        super.finish();
        if (mFinishWithAnimation) {
            overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onDismissButtonClicked(View v) {
        finish();
    }

    public void gotoGP() {
        final String appPackageName = getPackageName();
        try {
            Intent launchIntent = new Intent();
            launchIntent.setPackage("com.android.vending");
            launchIntent.setData(Uri.parse("market://details?id=" + appPackageName));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopAnimationSpread();
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        super.onDestroy();
    }

}
