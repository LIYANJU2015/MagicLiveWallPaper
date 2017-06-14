package com.magiclive.ui;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.WallPaperUtils;
import com.magiclive.adapter.AdapterViewPager;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.VideoWallPaperDao;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.ui.base.BaseActivity;
import com.magiclive.util.DeviceUtils;
import com.magiclive.util.IntentUtils;
import com.magiclive.util.SizeUtils;
import com.magiclive.util.StatusBarColorCompat;
import com.magiclive.util.Utils;

import static com.magiclive.R.id.main_viewPager;
import static com.magiclive.db.VideoWallPaperDao.getVideoWallPaper;
import static com.magiclive.util.LogUtils.A;
import static com.magiclive.util.LogUtils.D;
import static com.magiclive.util.LogUtils.I;


/**
 * Created by liyanju on 2017/6/2.
 */

public class MainActivity extends BaseActivity {

    private TabLayout mMainTabLayout;

    private ViewPager mMainViewPager;

    private AdapterViewPager mAdapter;

    private CharSequence mTitle[] = {AppApplication.getContext().getString(R.string.live_wallpaper),
            AppApplication.getContext().getString(R.string.history_record)};

    private WallpaperHistoryFragment mHistroyFragment;
    private WallpaperListFragment mListFragment;

    private Context mContext;

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
        mAdapter.bindData(mTitle, mListFragment, mHistroyFragment);
        mMainViewPager.setAdapter(mAdapter);

        mMainTabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        mMainTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mMainTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mMainTabLayout.setupWithViewPager(mMainViewPager);
        mMainTabLayout.setSelectedTabIndicatorHeight(SizeUtils.dp2px(3));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_share:
                try {
                    mContext.startActivity(IntentUtils.getShareTextIntent(getString(R.string.share_content)));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_more:
                Intent intents = new Intent();
                intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intents.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                startActivity(intents);
                return true;
        }
        return false;
    }

    private AppCompatDialog mAppCompatDialog;

    private void showRatingDialog() {
        if (mAppCompatDialog != null && mAppCompatDialog.isShowing()) {
            mAppCompatDialog.dismiss();
        }
        View view = LayoutInflater.from(this).inflate(R.layout.rating_dialog_layout, null);
        ((AppCompatCheckBox)view.findViewById(R.id.rating_cb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppApplication.getSPUtils().put("isShowDialogRating", isChecked);
            }
        });
        view.findViewById(R.id.rate_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.gotoGP(mContext);
                mAppCompatDialog.dismiss();
                finish();
            }
        });
        view.findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppCompatDialog.dismiss();
                finish();
            }
        });

        mAppCompatDialog = new AppCompatDialog(this);
        mAppCompatDialog.setContentView(view);
        mAppCompatDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return false;
            }
        });
        mAppCompatDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (AppApplication.getSPUtils().getBoolean("isShowDialogRating", false)) {
            super.onBackPressed();
            return;
        }
        showRatingDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TransparentLiveWallPaperService.REQUEST_TRANSPERENT_CODE
                || requestCode == MirrorLiveWallPaperService.REQUEST_MIRROR_CODE) {
            if (resultCode == Activity.RESULT_OK
                    && DeviceUtils.getSDKVersion() >= Build.VERSION_CODES.LOLLIPOP) {
                ((AppApplication)getApplication()).getAppManager().killAll();

                if (AppApplication.isShowRatingDialog()) {
                    AppApplication.setShowRatingDialog();
                    RatingActivity.launch(getApplicationContext());
                }
            }
        }
    }
}
