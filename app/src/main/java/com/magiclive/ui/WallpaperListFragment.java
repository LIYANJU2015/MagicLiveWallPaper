package com.magiclive.ui;

import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magiclive.R;
import com.magiclive.WallPaperUtils;
import com.magiclive.bean.LiveWallPaperBean;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.SizeUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

import static android.R.attr.path;
import static com.magiclive.bean.LiveWallPaperBean.MIRROR_LIVE_WALLPAPER;
import static com.magiclive.bean.LiveWallPaperBean.VIDEO_LIVE_WALLPAPER;

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
        liveWallPaperBean.type = VIDEO_LIVE_WALLPAPER;
        mList.add(liveWallPaperBean);

        listRecyclerView.setAdapter(new CommonAdapter<LiveWallPaperBean>(mContext,
                R.layout.main_live_wallpaper_item, mList) {
            @Override
            protected void convert(ViewHolder holder, final LiveWallPaperBean bean, int position) {
                holder.setText(R.id.title, bean.title);

                if (bean.type == VIDEO_LIVE_WALLPAPER) {
                    if (bean.videoInfoBean == null) {
                        bean.setLastSDCardVideoInfo(mContext);
                    }
                    ImageView thumbnailIV = holder.getView(R.id.thumbnail);
                    if (bean.videoInfoBean != null && !TextUtils.isEmpty(bean.videoInfoBean.path)) {
                        Glide.with(mActivity).load(bean.videoInfoBean.path)
                                .placeholder(R.drawable.video_thumbnail_default)
                                .error(R.drawable.video_thumbnail_default).crossFade()
                                .into(thumbnailIV);
                        holder.getView(R.id.description).setVisibility(View.VISIBLE);
                        ((TextView)holder.getView(R.id.description)).setText(bean.videoInfoBean.path);
                    }
                    ((TextView)holder.getView(R.id.title)).setMaxLines(1);

                    holder.getView(R.id.thumbnail_frame).setBackgroundResource(R.drawable.video_icon_bg);
                    thumbnailIV.setPadding(0, 0, SizeUtils.dp2px(8), SizeUtils.dp2px(8));
                } else {
                    if (bean.type == MIRROR_LIVE_WALLPAPER) {
                        Glide.with(mActivity).load(R.drawable.mirror_icon)
                                .into((ImageView) holder.getView(R.id.thumbnail));
                    } else {
                        Glide.with(mActivity).load(R.drawable.transparency_icon)
                                .into((ImageView) holder.getView(R.id.thumbnail));
                    }

                    ((TextView)holder.getView(R.id.title)).setMaxLines(2);
                    holder.getView(R.id.description).setVisibility(View.GONE);
                }

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
                            case VIDEO_LIVE_WALLPAPER:
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
