package com.magiclive.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.magiclive.db.base.BaseContentProvider;
import com.magiclive.db.base.TableInfo;

/**
 * Created by liyanju on 2017/6/3.
 */

public class MagicLiveContentProvider extends BaseContentProvider{

    public static final int VIDEO_CODE = 111;

    @Override
    public void onAddTableInfo(SparseArray<TableInfo> tableInfoArray) {
        tableInfoArray.put(VIDEO_CODE, new MagicLiveContract.VideoContract());
    }

    @Override
    public String onDataBaseName() {
        return "magic_live";
    }

    @Override
    public int onDataBaseVersion() {
        return 1;
    }

    @Override
    public void onDBUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
