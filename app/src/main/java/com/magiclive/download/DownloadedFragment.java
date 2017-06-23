package com.magiclive.download;

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
import com.magiclive.bean.DownloadVideo;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.db.DownloadVideoDao;
import com.magiclive.db.MagicLiveContract;
import com.magiclive.ui.VideoWallPaperDetailActivity;
import com.magiclive.ui.base.BaseFragment;
import com.magiclive.util.FileUtils;

import static com.magiclive.util.LogUtils.I;

/**
 * Created by liyanju on 2017/6/22.
 */

public class DownloadedFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private DownloadingListViewAdapter adapter;

    @Override
    public void initView(View rootView) {
        ListView listView = (ListView)rootView.findViewById(R.id.download_listview);
        adapter = new DownloadingListViewAdapter(mContext);
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MagicLiveContract.DownloadVideoContract.VIDEO_STATUS + " == ?";
        String selectionArgs [] = new String[]{String.valueOf(DownloadVideo.COMPLETED)};
        return new CursorLoader(mContext,
                MagicLiveContract.DownloadVideoContract.CONTENT_URI, null,
                selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (adapter != null) {
            adapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public int onLayoutRes() {
        return R.layout.download_fragment_layout;
    }

    public class DownloadingListViewAdapter extends CursorAdapter {


        public DownloadingListViewAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.downloaded_item_layout, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final DownloadVideo downloadVideo = DownloadVideo.cursorToDownloadVideo(cursor);

            ImageView headIV = (ImageView)view.findViewById(R.id.thumbnail);
            TextView titleTV = (TextView)view.findViewById(R.id.title);
            TextView timeTV = (TextView)view.findViewById(R.id.time);
            TextView sizeTV = (TextView)view.findViewById(R.id.size_tv);
            View redPointView = view.findViewById(R.id.red_point);

            titleTV.setText(downloadVideo.title);
            timeTV.setText(downloadVideo.duration);
            Glide.with(mActivity).load(downloadVideo.imgUrl)
                    .placeholder(R.drawable.video_thumbnail_default)
                    .error(R.drawable.video_thumbnail_default).crossFade()
                    .into(headIV);
            sizeTV.setText(FileUtils.byte2FitMemorySize(downloadVideo.totalSize));
            if (downloadVideo.isVideoNew) {
                redPointView.setVisibility(View.VISIBLE);
            } else {
                redPointView.setVisibility(View.GONE);
            }

            view.findViewById(R.id.item_card_view).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    VideoWallPaperDetailActivity.launch(mContext,
                            VideoInfoBean.downloadVideoToVideoInfo(downloadVideo));
                    DownloadVideoDao.updateDownloadVideoNew(mContext, downloadVideo.id);
                }
            });
        }
    }
}
