package com.magiclive;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.magiclive.ui.MainActivity;
import com.magiclive.util.ProcessUtils;
import com.magiclive.util.SPUtils;
import com.magiclive.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import static android.R.attr.process;
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

        final String packageName = getPackageName();
        if (!TextUtils.isEmpty(packageName) && packageName.equals(getCurrentProcessName())) {
            CrashReport.initCrashReport(this, "d7855ed933", false);
        }

        if (!sSPUtils.getBoolean("Shortcut", false)) {
            sSPUtils.put("Shortcut", true);
            Utils.addShortcut(this, MainActivity.class, getString(R.string.app_name),
                    R.mipmap.ic_launcher);
        }
    }

    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> appProcessInfos = am.getRunningAppProcesses();

        if (appProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessInfos) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return "";
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
