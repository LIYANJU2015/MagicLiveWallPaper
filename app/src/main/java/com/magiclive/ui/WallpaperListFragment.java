package com.magiclive.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.magiclive.AdViewManager;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.WallPaperUtils;
import com.magiclive.bean.LiveWallPaperBean;
import com.magiclive.service.MirrorLiveWallPaperService;
import com.magiclive.service.TransparentLiveWallPaperService;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.LogUtils;
import com.magiclive.util.NetworkUtils;
import com.magiclive.util.ScreenUtils;
import com.magiclive.util.SizeUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

import java.util.ArrayList;


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
        listRecyclerView = (RecyclerView) rootView.findViewById(R.id.wallpaper_list_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void initData() {
        LiveWallPaperBean liveWallPaperBean;

        liveWallPaperBean = new LiveWallPaperBean();
        liveWallPaperBean.title = getString(R.string.video_wallpaper);
        liveWallPaperBean.type = VIDEO_LIVE_WALLPAPER;
        mList.add(liveWallPaperBean);

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

        HeaderAndFooterWrapper<LiveWallPaperBean> headerAndFooterWrapper = new HeaderAndFooterWrapper<>(mCommonAdapter);

        if (NetworkUtils.isAvailableByPing()) {
            headerAndFooterWrapper.addHeaderView(createNativeAdHeaderView());
        }
        listRecyclerView.setAdapter(headerAndFooterWrapper);
    }

    private View createNativeAdHeaderView() {
        LinearLayout linearLayout = new LinearLayout(mActivity);
        NativeExpressAdView adView = new NativeExpressAdView(mActivity);

        int adWidth;
        if (SizeUtils.px2dp(ScreenUtils.getScreenWidth()) > 1200) {
            adWidth = 1200;
        } else {
            adWidth = SizeUtils.px2dp(ScreenUtils.getScreenWidth());
        }
        adView.setAdSize(new AdSize(adWidth, 100));

        adView.setAdUnitId(getString(R.string.native_small_ad1));
        adView.setVideoOptions(new VideoOptions.Builder()
                .setStartMuted(true)
                .build());
        adView.loadAd(AdViewManager.createAdRequest());

        LinearLayout.LayoutParams childLayoutParams =
                new LinearLayout.LayoutParams(SizeUtils.dp2px(adWidth),
                        SizeUtils.dp2px(100));
        linearLayout.addView(adView, childLayoutParams);

        return linearLayout;
    }

    private Drawable getThumbnail(Uri aVideoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, aVideoUri);
        Bitmap bitmap = retriever
                .getFrameAtTime(1 * 1000 * 1000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        return drawable;
    }

    private CommonAdapter<LiveWallPaperBean> mCommonAdapter = new CommonAdapter<LiveWallPaperBean>(AppApplication.getContext(),
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
                    Uri uri = Uri.parse(bean.videoInfoBean.path);
                    LogUtils.v("xx", " convert uri "+ uri);
                    thumbnailIV.setImageDrawable(getThumbnail(uri));

                    holder.getView(R.id.description).setVisibility(View.GONE);
                } else {
                    holder.getView(R.id.thumbnail_frame).setBackground(null);
                }

                ((TextView) holder.getView(R.id.title)).setMaxLines(1);

            } else {
                if (bean.type == MIRROR_LIVE_WALLPAPER) {
                    ((ImageView) holder.getView(R.id.thumbnail)).setImageResource(R.drawable.mirror_icon);
                } else {
                    ((ImageView) holder.getView(R.id.thumbnail)).setImageResource(R.drawable.transparency_icon);
                }

                ((TextView) holder.getView(R.id.title)).setMaxLines(2);
                holder.getView(R.id.description).setVisibility(View.GONE);
            }

            holder.setOnClickListener(R.id.item_card_view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (bean.type) {
                        case LiveWallPaperBean.MIRROR_LIVE_WALLPAPER:
                            MirrorLiveWallPaperService.startMirrorWallpaperPreView(mActivity);
                            break;
                        case LiveWallPaperBean.TRANSPARENT_LIVE_WALLPAPER:
                            TransparentLiveWallPaperService.startTransparentWallpaperPreView(mActivity);
                            break;
                        case VIDEO_LIVE_WALLPAPER:
                            VideoWallPaperDetailActivity.launch(mContext, bean.videoInfoBean);
                            break;
                    }
                }
            });
        }
    };

    @Override
    public int onLayoutRes() {
        return R.layout.live_wallpaper_list;
    }
}
