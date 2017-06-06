package com.magiclive.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.magiclive.R;
import com.magiclive.WallPaperUtils;
import com.magiclive.bean.LiveWallPaperBean;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.ui.base.BaseFragment;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

/**
 * Created by liyanju on 2017/6/5.
 */

public class WallpaperListFragment extends BaseFragment {

    private RecyclerView listRecyclerView;

    private ArrayList<LiveWallPaperBean> mList = new ArrayList<>();

    @Override
    public void initView(View rootView) {
        listRecyclerView = (RecyclerView)rootView.findViewById(R.id.wallpaper_list_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void initData() {
        LiveWallPaperBean liveWallPaperBean;
        if (WallPaperUtils.isSupportFrontCamera()) {
            liveWallPaperBean = new LiveWallPaperBean();
            liveWallPaperBean.title = getString(R.string.mirror_wallpaper);
            liveWallPaperBean.type = LiveWallPaperBean.MIRROR_LIVE_WALLPAPER;
            mList.add(liveWallPaperBean);
        }

        liveWallPaperBean = new LiveWallPaperBean();
        liveWallPaperBean.title = getString(R.string.transperent_wallpaper);
        liveWallPaperBean.type = LiveWallPaperBean.TRANSPARENT_LIVE_WALLPAPER;
        mList.add(liveWallPaperBean);

        liveWallPaperBean = new LiveWallPaperBean();
        liveWallPaperBean.title = getString(R.string.video_wallpaper);
        liveWallPaperBean.type = LiveWallPaperBean.VIDEO_LIVE_WALLPAPER;
        mList.add(liveWallPaperBean);

        listRecyclerView.setAdapter(new CommonAdapter<LiveWallPaperBean>(mContext,
                R.layout.main_live_wallpaper_item, mList) {
            @Override
            protected void convert(ViewHolder holder, final LiveWallPaperBean bean, int position) {
                holder.setText(R.id.title, bean.title);
                holder.setOnClickListener(R.id.item_card_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (bean.type) {
                            case LiveWallPaperBean.MIRROR_LIVE_WALLPAPER:
                                MirrorLiveWallPaperService.startTransparentWallpaperPreView(mContext);
                                break;
                            case LiveWallPaperBean.TRANSPARENT_LIVE_WALLPAPER:
                                TransparentLiveWallPaperService.startTransparentWallpaperPreView(mContext);
                                break;
                            case LiveWallPaperBean.VIDEO_LIVE_WALLPAPER:
                                LocalVideoListActivity.launch(mContext);
                                break;
                        }
                    }
                });
            }
        });
    }

    @Override
    public int onLayoutRes() {
        return R.layout.live_wallpaper_list;
    }
}
