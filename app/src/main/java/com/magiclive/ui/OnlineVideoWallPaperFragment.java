package com.magiclive.ui;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magiclive.R;
import com.magiclive.bean.OnlineVideoWallPaper;
import com.magiclive.download.FileDownloaderHelper;
import com.magiclive.huoying.HuoyingHelper;
import com.magiclive.pexels.PexelsVideoHelper;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.LogUtils;
import com.magiclive.util.ThreadPoolUtils;
import com.magiclive.util.ToastUtils;
import com.magiclive.util.UIThreadHelper;
import com.paginate.Paginate;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

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

    private ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.SingleThread, 1);

    private String videoUrl = "";

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
            }
        };

        recyclerView.setAdapter(adapter);

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
                videoUrl = "";
                try {
                    videoUrl = HuoyingHelper.parseGetVideoUrlByDetailUrl(onlineVideo.detailUrl);
                    if (!TextUtils.isEmpty(videoUrl)) {
                        FileDownloaderHelper.getInstances().addDownloadTask(videoUrl,
                                onlineVideoWallPaper.currentUrl, onlineVideo.detailUrl);
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
                if (TextUtils.isEmpty(videoUrl)) {
                    ToastUtils.showShortToast(R.string.error_download);
                }
                UIThreadHelper.getInstance().getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter != null && isAdded()) {
                            adapter.notifyItemChanged(postion);
                        }
                    }
                }, 600);
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

                adapter.notifyItemRangeChanged(positionStart,
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
