package com.magiclive.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.adapter.AdapterViewPager;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.VideoWallPaperDao;
import com.magiclive.ui.base.BaseActivity;
import com.magiclive.util.SizeUtils;
import com.magiclive.util.StatusBarColorCompat;

import static com.magiclive.R.id.main_viewPager;
import static com.magiclive.db.VideoWallPaperDao.getVideoWallPaper;
import static com.magiclive.util.LogUtils.D;


/**
 * Created by liyanju on 2017/6/2.
 */

public class MainActivity extends BaseActivity {

    private TabLayout mMainTabLayout;

    private ViewPager mMainViewPager;

    private AdapterViewPager mAdapter;

    private CharSequence mTitle[] = {AppApplication.getContext().getString(R.string.history_record),
            AppApplication.getContext().getString(R.string.live_wallpaper)};

    private WallpaperHistoryFragment mHistroyFragment;
    private WallpaperListFragment mListFragment;

    private Context mContext;

    private AsyncTask mUpdateBGTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        mMainViewPager = (ViewPager) findViewById(main_viewPager);
        mAdapter = new AdapterViewPager(getSupportFragmentManager());
        mHistroyFragment = new WallpaperHistoryFragment();
        mListFragment = new WallpaperListFragment();
        mAdapter.bindData(mTitle, mHistroyFragment, mListFragment);
        mMainViewPager.setAdapter(mAdapter);

        mMainTabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        mMainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mMainTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mMainTabLayout.setupWithViewPager(mMainViewPager);
        mMainTabLayout.setSelectedTabIndicatorHeight(SizeUtils.dp2px(3));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBGThumbnail();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateBGTask != null) {
            mUpdateBGTask.cancel(true);
        }
    }

    private void updateBGThumbnail() {
        mUpdateBGTask = new AsyncTask<Void, Void,VideoInfoBean>(){
            @Override
            protected VideoInfoBean doInBackground(Void... params) {
                return VideoWallPaperDao.getVideoWallPaper(mContext);
            }

            @Override
            protected void onPostExecute(VideoInfoBean videoInfoBean) {
                super.onPostExecute(videoInfoBean);
                if (videoInfoBean != null && !MainActivity.this.isFinishing()) {
                    Glide.with(MainActivity.this).load(videoInfoBean.path).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            ViewCompat.setBackground(mMainViewPager,
                                    new BitmapDrawable(mContext.getResources(), resource));
                        }
                    });
                }
            }
        }.execute();
    }
}
