package com.magiclive;

import android.app.Application;
import android.content.Context;

import com.magiclive.util.SPUtils;
import com.magiclive.util.Utils;

import net.grandcentrix.tray.AppPreferences;

/**
 * Created by liyanju on 2017/6/3.
 */

public class AppApplication extends Application{

    private static SPUtils sSPUtils;

    private static Context sContext;

    private static AppPreferences sAppPreferences;

    public static SPUtils getSPUtils() {
        return sSPUtils;
    }

    public static AppPreferences getAppPreferences() {
        return sAppPreferences;
    }

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        Utils.init(this);

        sSPUtils = new SPUtils("magiclive");
        sAppPreferences = new AppPreferences(sContext);
    }
}
