package magiclivewall.wall.com.magiclivewall;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by liyanju on 2017/6/21.
 */
@RunWith(AndroidJUnit4.class)
public class PexelsVideoTest {

    @Test
    public void testGetPexelsList() {
        try {
            long firstTime = System.currentTimeMillis();
            Document document = Jsoup.connect("https://videos.pexels.com/popular-videos").get();
            Elements elements = document.select("a.video-preview ");
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String detailUrl = element.attr("abs:href");
                String duration = element.getElementsByClass("video-preview__duration").first().text();
                String title = element.getElementsByClass("video-preview__title").first().text();

                String imgUrl = element.getElementsByClass("video-preview__images").first()
                        .getElementsByClass("video-preview__image").last().attr("data-src");

                Log.v("xxx", " img detailUrl:: " + detailUrl
                        + " duration: " + duration
                        + " title: " + title
                        + " imgUrl :: " + imgUrl);

                parseDownloadUrl(detailUrl);
            }

            Elements nextElements = document.select("a.next_page");
            String nextUrl = nextElements.first().attr("abs:href");
            Log.v("xxx", " nextUrl >>" + nextUrl + " total time ::" + (System.currentTimeMillis() - firstTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDownloadUrl(String detailUrl) throws Exception{
        Document detailDocument = Jsoup.connect(detailUrl).get();
        String downloadUrl = detailDocument.select("span.js-download-tooltip > a")
                .first().attr("href");
        Log.v("xxx", "downloadUrl :: " + downloadUrl);
    }
}
