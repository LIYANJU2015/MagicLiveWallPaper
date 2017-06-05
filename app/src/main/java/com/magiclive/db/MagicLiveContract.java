package com.magiclive.db;

import android.content.ContentUris;
import android.net.Uri;

import com.magiclive.db.base.TableInfo;

import java.util.Map;

/**
 * Created by liyanju on 2017/6/3.
 */

public class MagicLiveContract {

    public static final String AUTHORITIES = "com.magiclive.db.MagicLiveContentProvider";

    public static class VideoContract extends TableInfo {

        public static final String VIDEO_NAME = "video_name";
        public static final String VIDEO_PATH = "video_path";
        public static final String VIDEO_SIZE = "video_size";
        public static final String VIDEO_DURATION = "video_duration";
        public static final String VIDEO_START_TIME = "video_start_time";
        public static final String VIDEO_END_TIME = "video_end_time";
        public static final String VIDEO_SELECT = "video_select";
        public static final String VIDEO_VOLUME = "video_volume";
        public static final String VIDEO_TIME = "video_time";

        public static final String TABLE_NAME = "VideoContract";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/" + TABLE_NAME);

        @Override
        public String onTableName() {
            return TABLE_NAME;
        }

        @Override
        public Uri onContentUri() {
            return CONTENT_URI;
        }

        @Override
        public void onInitColumnsMap(Map<String, String> columnsMap) {
            columnsMap.put(VIDEO_NAME, "text");
            columnsMap.put(VIDEO_PATH, "text");
            columnsMap.put(VIDEO_SIZE, "int");
            columnsMap.put(VIDEO_DURATION, "int");
            columnsMap.put(VIDEO_START_TIME, "int");
            columnsMap.put(VIDEO_END_TIME, "int");
            columnsMap.put(VIDEO_SELECT, "int");
            columnsMap.put(VIDEO_VOLUME, "int");
            columnsMap.put(VIDEO_TIME, "int");
        }
    }
}
