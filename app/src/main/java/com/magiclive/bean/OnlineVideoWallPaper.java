package com.magiclive.bean;

import com.magiclive.AppApplication;
import com.magiclive.commonloader.ComDataLoadTask;
import com.magiclive.commonloader.IComDataLoader;
import com.magiclive.db.DownloadVideoDao;
import com.magiclive.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static com.magiclive.db.DownloadVideoDao.getDownloadStatusById;

/**
 * Created by liyanju on 2017/6/19.
 */

public class OnlineVideoWallPaper {

    public String currentUrl;

    public String nextUrl;

    public ArrayList<OnlineVideo> onlineVideos = new ArrayList<>();

    public static class OnlineVideo implements IComDataLoader<Integer>{

        public String title;

        public String duration;

        public String detailUrl;

        public String imgUrl;

        public boolean isDownloadFinished;
        public int loadStatus;

        private static ArrayList<String> urlList = new ArrayList<>();

        public static void removeCache(String detailUrl) {
            urlList.remove(detailUrl);
        }

        public static void removeAll() {
            urlList.clear();
        }

        @Override
        public void setLoadSuccess() {

        }

        @Override
        public boolean isLoadSuccess() {
            return urlList.indexOf(detailUrl) > -1;
        }

        @Override
        public String getId() {
            return detailUrl;
        }

        @Override
        public Integer onHandleSelfData(ComDataLoadTask.AsyncHandleListener listener) {
            int downloadStatus = DownloadVideoDao.getDownloadStatusById(AppApplication.getContext(), detailUrl);
            LogUtils.v(" onHandleSelfData status " + downloadStatus + " detailUrl :: " + detailUrl);
            return downloadStatus;
        }

        @Override
        public void setLoadDataObj(Integer integer) {
            loadStatus = integer;
            isDownloadFinished = integer == DownloadVideo.COMPLETED;
            urlList.add(detailUrl);
        }

        @Override
        public Integer getLoadDataObj() {
            return loadStatus;
        }
    }

    public void update(OnlineVideoWallPaper oldVideoWallPaper) {
        currentUrl = oldVideoWallPaper.currentUrl;
        nextUrl = oldVideoWallPaper.nextUrl;
        onlineVideos.addAll(oldVideoWallPaper.onlineVideos);
    }

}
