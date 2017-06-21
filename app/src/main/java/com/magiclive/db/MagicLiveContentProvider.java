package com.magiclive.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.magiclive.db.base.BaseContentProvider;
import com.magiclive.db.base.TableInfo;
import com.magiclive.util.LogUtils;

/**
 * Created by liyanju on 2017/6/3.
 */

public class MagicLiveContentProvider extends BaseContentProvider{

    public static final int VIDEO_CODE = 111;
    public static final int DOWNLOAD_VIDEO_CODE = 222;

    private SparseArray<TableInfo> tableInfoArray;

    @Override
    public void onAddTableInfo(SparseArray<TableInfo> tableInfoArray) {
        this.tableInfoArray = tableInfoArray;
        tableInfoArray.put(VIDEO_CODE, new MagicLiveContract.VideoContract());
        tableInfoArray.put(DOWNLOAD_VIDEO_CODE, new MagicLiveContract.DownloadVideoContract());
    }

    @Override
    public String onDataBaseName() {
        return "magic_live";
    }

    //1.1 1
    //1.2 2
    @Override
    public int onDataBaseVersion() {
        return 2;
    }

    @Override
    public void onDBUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.v("onDBUpgrade oldVersion " + oldVersion + " newVersion " + newVersion);
        if (oldVersion == 1) {
            db.execSQL(tableInfoArray.get(DOWNLOAD_VIDEO_CODE).getCreateTableSql());
        }
    }
}
