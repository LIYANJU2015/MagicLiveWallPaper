package com.magiclive.ui.base;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.magiclive.R;
import com.magiclive.util.LogUtils;
import com.magiclive.util.StatusBarColorCompat;

/**
 * Created by liyanju on 2017/6/5.
 */

public class BaseActivity extends AppCompatActivity {

    private View mViewBar;

    private ObjectAnimator mStatusBarColorAnimator;

    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    private boolean mActionBarShown = true;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        StatusBarColorCompat.setColor(this, R.color.colorPrimaryDark);
    }

    public void setCurToolbar(View viewbar) {
        mViewBar = viewbar;
    }

    public void onBaseScrolled(RecyclerView recyclerView, int dx, int dy) {
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        //show views if first item is first visible position and views are hidden
        if (firstVisibleItem == 0) {
            if(!controlsVisible) {
                autoShowOrHideActionBar(true);
                controlsVisible = true;
            }
        } else {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                autoShowOrHideActionBar(false);
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                autoShowOrHideActionBar(true);
                controlsVisible = true;
                scrolledDistance = 0;
            }
        }

        if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
            scrolledDistance += dy;
        }
    }

    protected void autoShowOrHideActionBar(boolean show) {
        LogUtils.v("autoShowOrHideActionBar show " + show);
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mViewBar == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mStatusBarColorAnimator != null) {
                mStatusBarColorAnimator.cancel();
            }
            mStatusBarColorAnimator = ObjectAnimator.ofInt(getWindow(), "statusBarColor",
                    shown ? Color.BLACK : getResources().getColor(R.color.theme_primary_dark),
                    shown ? getResources().getColor(R.color.theme_primary_dark) : Color.BLACK)
            .setDuration(250);
            mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
            mStatusBarColorAnimator.start();
        }


            if (shown) {
                ViewCompat.animate(mViewBar)
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator(2))
                        // Setting Alpha animations should be done using the
                        // layer_type set to layer_type_hardware for the duration of the animation.
                        .withLayer();
            } else {
                ViewCompat.animate(mViewBar)
                        .translationY(-mViewBar.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator(2))
                        // Setting Alpha animations should be done using the
                        // layer_type set to layer_type_hardware for the duration of the animation.
                        .withLayer();
            }

    }
}
