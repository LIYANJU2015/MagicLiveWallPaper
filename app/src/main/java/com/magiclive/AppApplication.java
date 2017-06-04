package com.magiclive;

import android.app.Application;
import android.content.Context;

import com.magiclive.util.SPUtils;
import com.magiclive.util.Utils;


/**
 * Created by liyanju on 2017/6/3.
 */

public class AppApplication extends Application{

    private static SPUtils sSPUtils;

    private static Context sContext;

    public static SPUtils getSPUtils() {
        return sSPUtils;
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
    }
}
