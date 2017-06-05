package com.magiclive;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Camera;

import static android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER;

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
            intent = new Intent(ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
        }
        return intent;
    }

    public static int findFrontCamera(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

    public static boolean isSupportFrontCamera() {
        return findFrontCamera() != -1;
    }
}
