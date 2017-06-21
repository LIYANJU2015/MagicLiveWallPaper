package com.magiclive.pexels;

import android.util.Log;

import com.magiclive.bean.OnlineVideoWallPaper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by liyanju on 2017/6/21.
 */

public class PexelsVideoHelper {

    public static final String HOST_URL = "https://videos.pexels.com/popular-videos";

    public static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";

    public static OnlineVideoWallPaper getPexelsVideoWallPaper(String url) throws Exception {
        Document document = connectGet(url);
        OnlineVideoWallPaper onlineVideoWallPaper = new OnlineVideoWallPaper();
        parsePexelsVideoPager(document, onlineVideoWallPaper);
        return onlineVideoWallPaper;
    }

    private static void parsePexelsVideoPager(Document document, OnlineVideoWallPaper onlineVideoWallPaper) {
        Elements elements = document.select("a.video-preview ");
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            String detailUrl = element.attr("abs:href");
            String duration = element.getElementsByClass("video-preview__duration").first().text();
            String title = element.getElementsByClass("video-preview__title").first().text();
            String imgUrl = element.getElementsByClass("video-preview__images").first()
                    .getElementsByClass("video-preview__image").last().attr("data-src");

            OnlineVideoWallPaper.OnlineVideo onlineVideo = new OnlineVideoWallPaper.OnlineVideo();
            onlineVideo.imgUrl = imgUrl;
            onlineVideo.detailUrl = detailUrl;
            onlineVideo.title = title;
            onlineVideo.duration = duration;
            onlineVideoWallPaper.onlineVideos.add(onlineVideo);

            Log.v("xxx", " img detailUrl:: " + detailUrl
                    + " duration: " + duration
                    + " title: " + title
                    + " imgUrl :: " + imgUrl);
        }

        Elements nextElements = document.select("a.next_page");
        onlineVideoWallPaper.nextUrl = nextElements.first().attr("abs:href");
    }

    private static Document connectGet(String url) throws Exception {
        return Jsoup.connect(url).userAgent(UA).get();
    }

    public static String getDownloadVideoUrl(String detailUrl) throws Exception{
        Document detailDocument = Jsoup.connect(detailUrl).get();
        String downloadUrl = detailDocument.select("span.js-download-tooltip > a")
                .first().attr("href");
        Log.v("xxx", "downloadUrl :: " + downloadUrl);
        return downloadUrl;
    }
}
