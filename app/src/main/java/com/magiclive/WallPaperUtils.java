package com.magiclive;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;

/**
 * Created by liyanju on 2017/6/2.
 */

public class WallPaperUtils {

    public static Intent createLiveWallpaperIntent(String packageName, String classFullName) {
        ComponentName componentName = new ComponentName(packageName, classFullName);
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT < 16) {
            intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        } else {
            intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
        }
        return intent;
    }
}
