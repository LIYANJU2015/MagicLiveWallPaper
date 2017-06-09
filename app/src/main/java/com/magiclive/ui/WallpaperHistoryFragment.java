package com.magiclive.ui;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magiclive.AppApplication;
import com.magiclive.R;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.service.VideoLiveWallPaperService;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.FileUtils;
import com.magiclive.util.TimeUtils;



/**
 * Created by liyanju on 2017/6/5.
 */

public class WallpaperHistoryFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mHistoryListView;
    private WallPaperHistoryAdapter mAdapter;

    @Override
    public void initView(View rootView) {
        mHistoryListView = (ListView)rootView.findViewById(R.id.history_listview);
        mAdapter = new WallPaperHistoryAdapter(mContext);
        mHistoryListView.setAdapter(mAdapter);
        mHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfoBean videoInfoBean = new VideoInfoBean();
                if (mHeaderView != null) {
                    position--;
                }
                videoInfoBean.cursorToVideoInfoBean((Cursor) mAdapter.getItem(position));
                VideoWallPaperDetailActivity.launch(mContext, videoInfoBean);
            }
        });

        mHistoryListView.setEmptyView(rootView.findViewById(R.id.empty_frame));

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public int onLayoutRes() {
        return R.layout.live_wallpaper_history_layout;
    }

    private View mHeaderView;

    private void initHeaderView() {
        final TextView audioTV = (TextView)mHeaderView.findViewById(R.id.audio_tv);
        SwitchCompat audioSwitch = (SwitchCompat)mHeaderView.findViewById(R.id.audio_switch);
        audioSwitch.setChecked(AppApplication.getSPUtils().getBoolean("audio", true));
        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    VideoLiveWallPaperService.setVideoWallpaperVolume(mContext, false);
                    audioTV.setText(getString(R.string.audio_enabled));
                } else {
                    VideoLiveWallPaperService.setVideoWallpaperVolume(mContext, true);
                    audioTV.setText(getString(R.string.audio_disbled));
                }
                AppApplication.getSPUtils().put("audio", isChecked);
            }
        });

        final TextView scaleTV = (TextView)mHeaderView.findViewById(R.id.scale_tv);
        SwitchCompat scaleSwitch = (SwitchCompat)mHeaderView.findViewById(R.id.scale_switch);
        scaleSwitch.setChecked(AppApplication.getSPUtils().getBoolean("scale", true));
        scaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    VideoLiveWallPaperService.setScaleVideoWallpaper(mContext, true);
                    scaleTV.setText(getString(R.string.scale_fit));
                } else {
                    VideoLiveWallPaperService.setScaleVideoWallpaper(mContext, false);
                    scaleTV.setText(getString(R.string.scale_fit_with_crop));
                }
                AppApplication.getSPUtils().put("scale", isChecked);
            }
        });
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            if (data != null && data.getCount() > 0) {
                if (mHeaderView == null) {
                    mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.history_header_layout, null);
                    initHeaderView();
                    mHistoryListView.addHeaderView(mHeaderView);
                }
            } else {
                if (mHeaderView != null) {
                    try {
                        mHistoryListView.removeHeaderView(mHeaderView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            mAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, MagicLiveContract.VideoContract.CONTENT_URI,
                null, null, null, MagicLiveContract.VideoContract.VIDEO_TIME + " desc ");
    }

    public class WallPaperHistoryAdapter extends CursorAdapter {

        private class ViewHolder {

            public TextView titleTV;
            public ImageView thumbnailIV;
            public ImageView playIconIV;
            public TextView descriptionTV;
            public TextView sizeTV;
            public TextView timeTV;
        }

        public WallPaperHistoryAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            View view  = LayoutInflater.from(mContext).inflate(R.layout.main_live_wallpaper_history_item, null);
            viewHolder.thumbnailIV = (ImageView) view.findViewById(R.id.thumbnail);
            viewHolder.titleTV = (TextView)view.findViewById(R.id.title);
            viewHolder.playIconIV = (ImageView)view.findViewById(R.id.play_icon_iv);
            viewHolder.descriptionTV = (TextView) view.findViewById(R.id.description);
            viewHolder.sizeTV = (TextView)view.findViewById(R.id.size);
            viewHolder.timeTV = (TextView) view.findViewById(R.id.time);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.titleTV.setText(VideoInfoBean.getName(cursor));

            if (VideoInfoBean.isSelection(cursor)) {
                viewHolder.playIconIV.setVisibility(View.VISIBLE);
            } else {
                viewHolder.playIconIV.setVisibility(View.GONE);
            }

            viewHolder.timeTV.setText(TimeUtils.stringForTime((int)VideoInfoBean.getDuration(cursor)));
            viewHolder.timeTV.setVisibility(View.VISIBLE);

            viewHolder.descriptionTV.setText(VideoInfoBean.getPath(cursor));
            viewHolder.sizeTV.setText(FileUtils.byte2FitMemorySize(VideoInfoBean.getSize(cursor)));

            Glide.with(mActivity).load(VideoInfoBean.getPath(cursor))
                    .placeholder(R.drawable.video_thumbnail_default)
                    .error(R.drawable.video_thumbnail_default).crossFade()
                    .into(viewHolder.thumbnailIV);
        }
    }
}
