package com.magiclive.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Process;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.magiclive.util.LogUtils;

import static com.magiclive.WallPaperUtils.createLiveWallpaperIntent;

/**
 * Created by liyanju on 2017/6/2.
 */

public class MirrorLiveWallPaperService extends WallpaperService{

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public Engine onCreateEngine() {
        return new MirrorEngine();
    }

    public static void startTransparentWallpaperPreView(Context context) {
        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                MirrorLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity)context).startActivityForResult(intent, 0);
        } else {
            context.startActivity(intent);
        }
    }

    public class MirrorEngine extends Engine implements Camera.PreviewCallback {

        private Camera mCamera;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            LogUtils.v("MirrorEngine","oncreate");
            startPreview();
            setTouchEventsEnabled(true);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopPreview();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            LogUtils.v("MirrorEngine","wallpager invisible " + visible + " myPid : " + Process.myPid());
            if (visible) {
                startPreview();
            } else {
                stopPreview();
            }
        }

        private int findFrontCamera(){
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

        public void startPreview() {

//            if(mCamera != null){
//                mCamera.setPreviewCallback(null);
//                mCamera.release();
//                mCamera = null;
//            }
            if (mCamera == null) {
                try {
                    int cameraId = findFrontCamera();
                    LogUtils.v("MirrorEngine", "wallpager startPreview cameraId " + cameraId);
                    mCamera = Camera.open(cameraId);
                    if (mCamera != null) {
                        final int orientation = context.getResources().getConfiguration().orientation;
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            mCamera.setDisplayOrientation(180);
                        } else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
                            mCamera.setDisplayOrientation(90);
                        }

                        mCamera.setPreviewDisplay(getSurfaceHolder());
                        mCamera.startPreview();
                    }
                } catch (Exception e) {
                    LogUtils.v("MirrorEngine","wallpager "+e.getMessage());
                }
            }
        }

        public void stopPreview() {
            if (mCamera != null) {
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);

                } catch (Exception e) {
                    LogUtils.v("MirrorEngine", "stopPreview Exception " + System.currentTimeMillis());
                } finally {
                    mCamera.release();
                    mCamera = null;
                }

                LogUtils.v("MirrorEngine", "wallpager stopPreview " + System.currentTimeMillis());
            }
        }


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mCamera.addCallbackBuffer(data);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            stopPreview();
            LogUtils.v("MirrorEngine", "onSurfaceDestroyed " + Process.myPid());
        }
    }
}
