package com.magiclive.ui;


import android.content.Context;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.magiclive.R;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.ui.base.BaseFragment;


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

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public int onLayoutRes() {
        return R.layout.live_wallpaper_history_layout;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
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
        }

        public WallPaperHistoryAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            View view  = LayoutInflater.from(mContext).inflate(R.layout.main_live_wallpaper_item, null);
            viewHolder.thumbnailIV = (ImageView) view.findViewById(R.id.thumbnail);
            viewHolder.titleTV = (TextView)view.findViewById(R.id.title);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.titleTV.setText(VideoInfoBean.getName(cursor));

            Glide.with(mActivity).load(VideoInfoBean.getPath(cursor))
                    .placeholder(R.drawable.video_thumbnail_default)
                    .error(R.drawable.video_thumbnail_default).crossFade()
                    .into(viewHolder.thumbnailIV);
        }
    }
}
