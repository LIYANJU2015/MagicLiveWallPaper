package com.magiclive.adapter;



import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.magiclive.R;
import com.magiclive.ui.MainActivity;

import java.util.Arrays;
import java.util.List;

public class AdapterViewPager extends FragmentPagerAdapter {
    private List<Fragment> mList;
    private CharSequence[] mTitles;

    public AdapterViewPager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void bindData(List<Fragment> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void bindData(List<Fragment> list, CharSequence[] titles) {
        this.mList = list;
        this.mTitles = titles;
        notifyDataSetChanged();
    }

    public void bindData(CharSequence[] titles, Fragment ...fragments) {
        this.mList = Arrays.asList(fragments);
        this.mTitles = titles;
        notifyDataSetChanged();
    }


    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null) {
            return mTitles[position];
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        View view = f.getView();
        if (view != null)
            container.addView(view);
        return f;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = mList.get(position).getView();
        if (view != null)
            container.removeView(view);
    }

    public View getTabView(Activity activity, int position) {
        View tab = LayoutInflater.from(activity).inflate(R.layout.custom_tab, null);
        TextView tv = (TextView) tab.findViewById(R.id.custom_text);
        tv.setText(mTitles[position]);
        return tab;
    }
}
