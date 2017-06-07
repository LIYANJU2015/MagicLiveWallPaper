package com.magiclive.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.TextView;

import com.lapism.searchview.SearchView;
import com.magiclive.R;
import com.magiclive.WallPaperUtils;
import com.magiclive.adapter.LocalVideoListAdapter;
import com.magiclive.ui.base.BaseActivity;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

/**
 * Created by liyanju on 2017/6/5.
 */

public class LocalVideoListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private RecyclerView mVideoListRecyclerView;
    private Context mContext;
    private LocalVideoListAdapter mAdapter;

    private HeaderAndFooterWrapper headerAdapter;

    private SearchView mSearchView;

    private String mSearchContent;
    private String mOrderContent;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_videolist_layout);
        mContext = getApplicationContext();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setToolbarElevation(getResources().getDimension(R.dimen.headerbar_elevation));

        setSupportActionBar(mToolbar);
        setCurToolbar(findViewById(R.id.headerbar));

        mVideoListRecyclerView = (RecyclerView)findViewById(R.id.video_list_recyclerview);
        mVideoListRecyclerView.setHasFixedSize(true);
        mVideoListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mVideoListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onBaseScrolled(recyclerView, dx, dy);
            }
        });

        mAdapter = new LocalVideoListAdapter(this);
        headerAdapter = new HeaderAndFooterWrapper(mAdapter);
        headerAdapter.addHeaderView(createHeaderView());
        mVideoListRecyclerView.setAdapter(headerAdapter);

        mSearchView = (SearchView)findViewById(R.id.searchView);
        mSearchView.setArrowOnly(false);
        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
        mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
        mSearchView.setTheme(SearchView.THEME_LIGHT, true);
        mSearchView.setHint(R.string.search);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String content) {
                selectionChangeRestartLoader(content, null);
                mSearchView.close(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String content) {
                selectionChangeRestartLoader(content, null);
                return false;
            }
        });
        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onClose() {
                setToolbarElevation(getResources().getDimension(R.dimen.headerbar_elevation));
                return false;
            }

            @Override
            public boolean onOpen() {
                setToolbarElevation(0);
                return false;
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void setToolbarElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(elevation);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return true;
    }

    private void selectionChangeRestartLoader(String content, String order) {
        if (!TextUtils.isEmpty(content)) {
            mSearchContent = content;
        }

        if (!TextUtils.isEmpty(order)) {
            mOrderContent = order;
        }
        getSupportLoaderManager().restartLoader(0, null, LocalVideoListActivity.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                mSearchView.open(true, item);
                return true;
            case R.id.action_sort_album:
                selectionChangeRestartLoader(null, "album ASC");
                 return true;
            case R.id.action_sort_date:
                selectionChangeRestartLoader(null, "datetaken DESC");
                return true;
            case R.id.action_sort_duration:
                selectionChangeRestartLoader(null, "duration ASC");
                return true;
            case R.id.action_sort_title:
                selectionChangeRestartLoader(null, "title ASC");
                return true;
            case R.id.action_sort_artist:
                selectionChangeRestartLoader(null, "artist ASC");
                return true;
            case R.id.menu_record_video:
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_SIZE_LIMIT,
                        768000);
                intent.putExtra(
                        android.provider.MediaStore.EXTRA_DURATION_LIMIT, 45000);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View createHeaderView() {
        View view = new View(this);
        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WallPaperUtils.getActionBarHeight(this));
        view.setLayoutParams(layoutParams);
        return view;
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, LocalVideoListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] strArr = new String[]{MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.DURATION};
        String selection = null;
        if (!TextUtils.isEmpty(mSearchContent)) {
            selection = "title LIKE \'%" + mSearchContent +"%\' OR "
                    + "album" + " LIKE \'%" + mSearchContent +"%\' OR "
                    + "artist" + " LIKE \'%"+ mSearchContent +"%\'";
        }
        return new CursorLoader(mContext, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, strArr, selection,
                null, mOrderContent);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null && headerAdapter != null) {
            mAdapter.changeCursor(data);
            headerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
