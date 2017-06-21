package com.magiclive.bean;

import java.util.ArrayList;

/**
 * Created by liyanju on 2017/6/19.
 */

public class OnlineVideoWallPaper {

    public String currentUrl;

    public String nextUrl;

    public ArrayList<OnlineVideo> onlineVideos = new ArrayList<>();

    public static class OnlineVideo {

        public String title;

        public String duration;

        public String detailUrl;

        public String imgUrl;
    }

    public void update(OnlineVideoWallPaper oldVideoWallPaper) {
        currentUrl = oldVideoWallPaper.currentUrl;
        nextUrl = oldVideoWallPaper.nextUrl;
        onlineVideos.addAll(oldVideoWallPaper.onlineVideos);
    }

}
