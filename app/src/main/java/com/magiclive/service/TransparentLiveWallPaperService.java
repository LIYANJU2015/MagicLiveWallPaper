package com.magiclive.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class TransparentLiveWallPaperService extends WallpaperService{

    public static final int REQUEST_TRANSPERENT_CODE = 222;

    @Override
    public Engine onCreateEngine() {
        return new TransparentEngine();
    }

    public static void startTransparentWallpaperPreView(Context context) {
        Intent intent = createLiveWallpaperIntent(context.getPackageName(),
                TransparentLiveWallPaperService.class.getName());
        if (context instanceof Activity) {
            ((Activity)context).startActivityForResult(intent, REQUEST_TRANSPERENT_CODE);
        } else {
            context.startActivity(intent);
        }
    }

    public class TransparentEngine extends Engine implements Camera.PreviewCallback {

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
                LogUtils.v("MirrorEngine", "wallpager startPreview " + System.currentTimeMillis());

                try {
                    mCamera = Camera.open(0);
                    if (mCamera != null) {
                        mCamera.setDisplayOrientation(90);
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
