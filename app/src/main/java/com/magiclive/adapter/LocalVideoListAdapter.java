package com.magiclive.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.magiclive.R;
import com.magiclive.bean.VideoInfoBean;
import com.magiclive.ui.VideoWallPaperDetailActivity;
import com.magiclive.widget.RecyclerViewCursorAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import static com.magiclive.bean.VideoInfoBean.mediaInfoToVideoInfo;


/**
 * Created by liyanju on 2017/6/5.
 */

public class LocalVideoListAdapter extends RecyclerViewCursorAdapter<ViewHolder>{

    private Activity mActivity;
    private Context mContext;

    public LocalVideoListAdapter(Activity activity) {
        super(activity.getApplicationContext(), null, 0);
        mContext = activity.getApplicationContext();
        mActivity = activity;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final VideoInfoBean videoInfoBean = VideoInfoBean.mediaInfoToVideoInfo(cursor);
        holder.setText(R.id.title, videoInfoBean.name);

        ImageView thumbnailIV = holder.getView(R.id.thumbnail);
        Glide.with(mActivity).load(videoInfoBean.path)
                .placeholder(R.drawable.video_thumbnail_default)
                .error(R.drawable.video_thumbnail_default).crossFade()
                .into(thumbnailIV);

        holder.setOnClickListener(R.id.item_card_view, new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Cursor cursor = (Cursor) v.getTag();
                VideoWallPaperDetailActivity.launch(mContext, videoInfoBean);
            }
        });
    }

    @Override
    protected void onContentChanged() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.live_wallpaper_item, parent, false);
        return ViewHolder.createViewHolder(mContext, view);
    }
}
