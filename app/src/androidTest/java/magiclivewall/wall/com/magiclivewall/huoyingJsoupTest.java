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
 * Created by liyanju on 2017/6/19.
 */
@RunWith(AndroidJUnit4.class)
public class huoyingJsoupTest  {

    private void parseByDocument(Document document) {
        try {
            Elements elements = document.getElementsByClass("c cl");
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                Element linkUrlElement = element.getElementsByTag("a").get(0);
                String imgUrl = linkUrlElement.getElementsByTag("img").attr("src");
                Log.v("xx", "linkUrl " + linkUrlElement.attr("abs:href") + " imgUrl :: " + imgUrl);

                Document document1 = Jsoup.connect(linkUrlElement.attr("abs:href")).get();
                Elements elements22 = document1.select("a[href$=.mov]");
                if (elements22 == null || elements22.size() == 0) {
                    elements22 = document1.select("a[href$=.mp4]");
                }

                if (elements22 != null && elements22.size() != 0) {
                    Log.v("xx", " linkUrl href: " + elements22.first().attr("href"));
                }

            }

            Element nextElement = document.select("a.nxt").first();
            if (nextElement != null) {
                Log.v("xx", "next url:: " + nextElement.attr("abs:href"));
                document = Jsoup.connect(nextElement.attr("abs:href")).get();
                parseByDocument(document);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.v("xx", " exception :: " + e.getMessage());
        }
    }

    @Test
    public void testGetAllLiveWallPaper() {
        try {
            long startTime = System.currentTimeMillis();

            Document document = Jsoup.connect("http://bbs.huoying666.com/forum-53-1.html").get();
            // document.setBaseUri("http://bbs.huoying666.com/");

            parseByDocument(document);


            Log.v("xx", "linkUrl end time222 : " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
