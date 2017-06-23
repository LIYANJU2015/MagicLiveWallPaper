package com.magiclive.ui;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.magiclive.R;
import com.magiclive.db.DownloadVideoDao;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

/**
 * Created by liyanju on 2017/6/21.
 */

public class DownloadActionProvider extends ActionProvider{

    private ImageView mIvIcon;
    private int clickWhat;
    private OnClickListener onClickListener;

    private Badge mBadge;

    public DownloadActionProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
//        int size = getContext().getResources().getDimensionPixelSize(
//                android.support.design.R.dimen.abc_action_bar_default_height_material);

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(size, size);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.download_badge_layout, null, false);

//        view.setLayoutParams(layoutParams);
        mIvIcon = (ImageView) view.findViewById(R.id.iv_icon);
        view.setOnClickListener(onViewClickListener);

        mBadge = new QBadgeView(getContext()).bindTarget(mIvIcon);
        mBadge.setGravityOffset(-2, true);

        int count = DownloadVideoDao.getDownloadNewCount();
        if (count > 0) {
            mBadge.setBadgeNumber(count);
        }
        return view;
    }

    public void setBadge(int i) {
        mBadge.setBadgeNumber(i);
    }

    private View.OnClickListener onViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onClickListener != null)
                onClickListener.onClick(clickWhat);
        }
    };

    public void setOnClickListener(int what, OnClickListener onClickListener) {
        this.clickWhat = what;
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int what);
    }
}
