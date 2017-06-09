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

import com.magiclive.WallPaperUtils;
import com.magiclive.util.LogUtils;

import static com.magiclive.WallPaperUtils.createLiveWallpaperIntent;

/**
 * Created by liyanju on 2017/6/2.
 */

public class MirrorLiveWallPaperService extends WallpaperService{

    private Context context;

    public static final int REQUEST_MIRROR_CODE = 555;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public Engine onCreateEngine() {
        return new MirrorEngine();
    }

    public static void startMirrorWallpaperPreView(Context context) {
        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                MirrorLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity)context).startActivityForResult(intent, REQUEST_MIRROR_CODE);
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

        public void startPreview() {

//            if(mCamera != null){
//                mCamera.setPreviewCallback(null);
//                mCamera.release();
//                mCamera = null;
//            }
            if (mCamera == null) {
                try {
                    int cameraId = WallPaperUtils.findFrontCamera();
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
