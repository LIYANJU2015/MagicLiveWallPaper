package com.magiclive.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.magiclive.R;
import com.magiclive.adapter.LocalVideoListAdapter;

/**
 * Created by liyanju on 2017/6/5.
 */

public class LocalVideoListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private RecyclerView mVideoListRecyclerView;
    private Context mContext;
    private LocalVideoListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_videolist_layout);
        mContext = getApplicationContext();

        mVideoListRecyclerView = (RecyclerView)findViewById(R.id.video_list_recyclerview);
        mVideoListRecyclerView.setHasFixedSize(true);
        mVideoListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mAdapter = new LocalVideoListAdapter(this);
        mVideoListRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, LocalVideoListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] strArr = new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE};
        return new CursorLoader(mContext, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, strArr, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            mAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
