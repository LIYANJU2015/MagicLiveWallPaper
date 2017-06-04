package com.magiclive;

import android.app.Application;
import android.content.Context;

import com.magiclive.util.SPUtils;
import com.magiclive.util.Utils;

import static com.magiclive.util.LogUtils.A;


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
    }
}
