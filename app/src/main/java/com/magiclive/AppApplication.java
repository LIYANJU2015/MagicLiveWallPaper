package com.magiclive;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.magiclive.util.SPUtils;
import com.magiclive.util.Utils;

import static com.magiclive.util.LogUtils.A;
import static java.lang.System.currentTimeMillis;


/**
 * Created by liyanju on 2017/6/3.
 */

public class AppApplication extends Application{

    private static SPUtils sSPUtils;

    private static Context sContext;

    private AppManager appManager;

    public static SPUtils getSPUtils() {
        return sSPUtils;
    }

    public static Context getContext() {
        return sContext;
    }

    public AppManager getAppManager() {
        return appManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        Utils.init(this);

        sSPUtils = new SPUtils("magiclive");

        appManager = new AppManager(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycle(appManager));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//如果是4.4及以上版本
            try {
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isShowRatingDialog() {
        if (sSPUtils.getLong("time", 0) != 0) {
            long duraton = System.currentTimeMillis() - sSPUtils.getLong("time", 0);
            if (duraton < 1000 * 60 * 30) {
                return false;
            }
        }

        return sSPUtils.getInt("ratingcount", 0) < 4;
    }


    public static void setShowRatingDialog() {
        int count = sSPUtils.getInt("ratingcount", 0);
        count++;
        sSPUtils.put("time", currentTimeMillis());
        sSPUtils.put("ratingcount", count);
    }
}
