package com.magiclive.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.magiclive.AdViewManager;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.adapter.AdapterViewPager;
import com.magiclive.ui.base.BaseActivity;
import com.magiclive.util.ScreenUtils;
import com.magiclive.util.SizeUtils;

/**
 * Created by liyanju on 2017/6/22.
 */

public class DownloadVideoActvity extends BaseActivity {

    private String title[] = new String[]{
            AppApplication.getContext().getString(R.string.download),
            AppApplication.getContext().getString(R.string.downloaded)
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_video_layout);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.download_tablayout);
        tabLayout.setVisibility(View.GONE);

        ViewPager viewPager = (ViewPager) findViewById(R.id.download_viewPager);

        AdapterViewPager adapter = new AdapterViewPager(getSupportFragmentManager());
        adapter.bindData(title, new DownloadedFragment());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(SizeUtils.dp2px(3));
    }

    @Override
    public boolean isAddNavigationBackIcon() {
        return true;
    }

    @Override
    public void onSetToolbar(ActionBar toolbar) {
        toolbar.setTitle(R.string.download);
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, DownloadVideoActvity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
