package com.magiclive.ui;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.magiclive.AdViewManager;
import com.magiclive.R;
import com.magiclive.bean.DownloadVideo;
import com.magiclive.bean.LiveWallPaperBean;
import com.magiclive.bean.OnlineVideoWallPaper;
import com.magiclive.commonloader.AsyncComDataLoader;
import com.magiclive.commonloader.IComDataLoader;
import com.magiclive.commonloader.IComDataLoaderListener;
import com.magiclive.download.FileDownloaderHelper;
import com.magiclive.pexels.PexelsVideoHelper;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.LogUtils;
import com.magiclive.util.NetworkUtils;
import com.magiclive.util.ScreenUtils;
import com.magiclive.util.SizeUtils;
import com.magiclive.util.ToastUtils;
import com.paginate.Paginate;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

/**
 * Created by liyanju on 2017/6/19.
 */

public class OnlineVideoWallPaperFragment extends BaseFragment implements Paginate.Callbacks {

    private static final String TAG = "OnlineVideoWallPaper";

    private OnlineVideoWallPaper onlineVideoWallPaper = new OnlineVideoWallPaper();

    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    private Paginate mPaginate;

    private boolean mIsLoadingMore;

    private CommonAdapter<OnlineVideoWallPaper.OnlineVideo> adapter;

    private TextView emptyTipsTV;

    private String downloadUrl = "";

    private HeaderAndFooterWrapper<LiveWallPaperBean> headerAndFooterWrapper;

    public void updateAdapter() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private View createNativeAdHeaderView() {
        LinearLayout linearLayout = new LinearLayout(mActivity);
        NativeExpressAdView adView = new NativeExpressAdView(mContext);

        int adWidth;
        if (SizeUtils.px2dp(ScreenUtils.getScreenWidth()) > 1200) {
            adWidth = 1200;
        } else {
            adWidth = SizeUtils.px2dp(ScreenUtils.getScreenWidth());
        }
        adView.setAdSize(new AdSize(adWidth, 100));

        adView.setAdUnitId(getString(R.string.native_small_online_video));
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

    @Override
    public void initView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));

        emptyTipsTV = (TextView)rootView.findViewById(R.id.empty_tips_tv);
        emptyTipsTV.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        emptyTipsTV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                initData();
            }
        });

        adapter = new CommonAdapter<OnlineVideoWallPaper.OnlineVideo>(mContext,
                R.layout.online_videopaper_item, onlineVideoWallPaper.onlineVideos) {
            @Override
            protected void convert(ViewHolder holder, final OnlineVideoWallPaper.OnlineVideo onlineVideo, final int position) {
                ImageView imageView = holder.getView(R.id.online_iv);
                Glide.with(mActivity).load(onlineVideo.imgUrl)
                        .placeholder(R.drawable.video_thumbnail_default)
                        .error(R.drawable.video_thumbnail_default).crossFade()
                        .into(imageView);

                TextView titleTV = holder.getView(R.id.title_tv);
                titleTV.setText(onlineVideo.title);

                TextView timeTV = holder.getView(R.id.time_tv);
                timeTV.setText(onlineVideo.duration);

                TextView downloadTV = holder.getView(R.id.download_status);

                holder.getView(R.id.download_status).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        downloadVideoWallpaper(onlineVideo, position);
                    }
                });

                downloadTV.setText(R.string.download);
                AsyncComDataLoader.getInstance().display(new IComDataLoaderListener() {
                    @Override
                    public void onLoadingComplete(IComDataLoader infoLoader, View... views) {
                        int downloadStatus = (int)infoLoader.getLoadDataObj();
                        TextView downloadTV = (TextView) views[0];
                        if (downloadStatus == DownloadVideo.COMPLETED) {
                            downloadTV.setText(R.string.downloaded);
                        }
                    }

                    @Override
                    public void onCancelLoading(View... views) {

                    }

                    @Override
                    public void onStartLoading(View... views) {

                    }
                }, onlineVideo, downloadTV);
            }
        };

        headerAndFooterWrapper = new HeaderAndFooterWrapper<>(adapter);

        if (NetworkUtils.isAvailableByPing()) {
            headerAndFooterWrapper.addHeaderView(createNativeAdHeaderView());
        }

        recyclerView.setAdapter(headerAndFooterWrapper);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    }

    private void downloadVideoWallpaper(final OnlineVideoWallPaper.OnlineVideo onlineVideo, final int postion) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                downloadUrl = "";
                try {
                    downloadUrl = PexelsVideoHelper.getDownloadVideoUrl(onlineVideo.detailUrl);
                    LogUtils.v(TAG, "downloadVideoWallpaper downloadUrl " + downloadUrl);
                    if (!TextUtils.isEmpty(downloadUrl)) {
                        FileDownloaderHelper.getInstances().addDownloadTask(downloadUrl, onlineVideo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressBar.setVisibility(View.GONE);
                if (TextUtils.isEmpty(downloadUrl)) {
                    ToastUtils.showShortToast(R.string.error_download);
                }

                if (adapter != null && isAdded()) {
                    adapter.notifyItemChanged(postion);
                }
            }
        }.execute();
    }

    @Override
    public void initData() {
        new AsyncTask<OnlineVideoWallPaper, Void, OnlineVideoWallPaper>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mIsLoadingMore = true;
                if (onlineVideoWallPaper.onlineVideos.size() == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected OnlineVideoWallPaper doInBackground(OnlineVideoWallPaper... params) {
                OnlineVideoWallPaper onlineVideoWallPaper = null;
                try {
                    OnlineVideoWallPaper onlineVideoWallPaper1 = params[0];
                    String url = PexelsVideoHelper.HOST_URL;
                    if (!TextUtils.isEmpty(onlineVideoWallPaper1.nextUrl)) {
                        url = onlineVideoWallPaper1.nextUrl;
                    }
                    LogUtils.v(TAG, "getPexelsVideoWallPaper URL " + url);
                    onlineVideoWallPaper = PexelsVideoHelper.getPexelsVideoWallPaper(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return onlineVideoWallPaper;
            }

            @Override
            protected void onPostExecute(OnlineVideoWallPaper videoWallPaper) {
                super.onPostExecute(videoWallPaper);
                mIsLoadingMore = false;
                if (videoWallPaper == null || videoWallPaper.onlineVideos.size() == 0) {
                    if (onlineVideoWallPaper.onlineVideos.size() == 0) {
                        emptyTipsTV.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    return;
                } else {
                    emptyTipsTV.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);

                if (!isAdded()) {
                    return;
                }

                int positionStart = onlineVideoWallPaper.onlineVideos.size();
                onlineVideoWallPaper.update(videoWallPaper);

                headerAndFooterWrapper.notifyItemRangeChanged(positionStart,
                        onlineVideoWallPaper.onlineVideos.size());

                if (mPaginate == null && onlineVideoWallPaper.onlineVideos.size() > 0) {
                    mPaginate = Paginate.with(recyclerView, OnlineVideoWallPaperFragment.this)
                            .setLoadingTriggerThreshold(0)
                            .build();
                    mPaginate.setHasMoreDataToLoad(false);
                }

            }
        }.execute(onlineVideoWallPaper);
    }

    @Override
    public int onLayoutRes() {
        return R.layout.online_videowallpaper;
    }

    @Override
    public void onLoadMore() {
        LogUtils.v(TAG, "onLoadMore >>>");
        initData();
    }

    @Override
    public boolean isLoading() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return false;
    }
}
