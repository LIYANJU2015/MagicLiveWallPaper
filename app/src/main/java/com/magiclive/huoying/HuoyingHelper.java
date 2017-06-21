package com.magiclive.huoying;

import com.magiclive.bean.OnlineVideoWallPaper;
import com.magiclive.util.LogUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by liyanju on 2017/6/19.
 */

public class HuoyingHelper {

    public static final String HOST_URL = "http://bbs.huoying666.com/forum-53-1.html";

    public static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";

    public static OnlineVideoWallPaper getHuoyingVideoWallPaper(String url) throws Exception {
        LogUtils.v("HuoyingHelper", "getHuoyingVideoWallPaper URL " + url);
        Document document = connectGet(url);

        OnlineVideoWallPaper onlineVideoWallPaper = new OnlineVideoWallPaper();
        parseVideoWallPaperPage(document, onlineVideoWallPaper, url);

        return onlineVideoWallPaper;
    }

    private static Document connectGet(String url) throws Exception {
        return Jsoup.connect(url).userAgent(UA).get();
    }

    private static void parseVideoWallPaperPage(Document document,
                                                OnlineVideoWallPaper videoWallPaper,
                                                String requestUrl) throws Exception {
        Elements elements = document.getElementsByClass("c cl");
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            Element linkUrlElement = element.getElementsByTag("a").get(0);
            String imgUrl = linkUrlElement.getElementsByTag("img").attr("src");

            OnlineVideoWallPaper.OnlineVideo onlineVideo = new OnlineVideoWallPaper.OnlineVideo();
            onlineVideo.detailUrl = linkUrlElement.attr("abs:href");
            onlineVideo.imgUrl = imgUrl;
            videoWallPaper.onlineVideos.add(onlineVideo);
        }

        Element nextElement = document.select("a.nxt").first();
        if (nextElement != null) {
            videoWallPaper.nextUrl = nextElement.attr("abs:href");
        }

        videoWallPaper.currentUrl = requestUrl;
    }

    public static String parseGetVideoUrlByDetailUrl(String url) throws Exception {
        Document document1 = connectGet(url);
        Elements elements22 = document1.select("a[href$=.mov]");
        if (elements22 == null || elements22.size() == 0) {
            elements22 = document1.select("a[href$=.mp4]");
        }

        if (elements22 != null && elements22.size() != 0) {
            LogUtils.v("video url href: " + elements22.first().attr("href"));
            return elements22.first().attr("href");
        }
        return null;
    }
}
