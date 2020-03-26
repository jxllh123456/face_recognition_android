package com.anloq.utils;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomCamera implements Camera.PictureCallback, Camera.PreviewCallback {

    private static final String TAG = CustomCamera.class.getSimpleName();
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private Camera mCamera;
    private SurfaceHolder mHolder;

    private static CustomCamera instance;

    private CustomCamera() {
    }

    public static synchronized CustomCamera getInstance() {
        if (instance == null)
            instance = new CustomCamera();
        return instance;
    }

    public void initCamera() {
        executor.execute(instance::openCamera);
    }

    public void setPreviewCallback() {
        mCamera.setPreviewCallbackWithBuffer(instance);
        mCamera.addCallbackBuffer(new byte[((1920 * 1080) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
    }

    private OnCameraInitListener onCameraInitListener;

    public interface OnCameraInitListener {
        void initSuccess(Camera camera);

        void onPreviewCallBack(byte[] data);
    }

    public void setOnCameraInitListener(OnCameraInitListener onCameraInitListener) {
        this.onCameraInitListener = onCameraInitListener;
    }

    public void removeCameraInitListener() {
        this.onCameraInitListener = null;
    }


    /**
     * 打开相机(默认为0)
     */
    private synchronized void openCamera() {
        // releaseCamera();
        try {
            // Camera.getNumberOfCameras();
            mCamera = Camera.open(1);

        } catch (Exception e) {
            Log.e(TAG, "fail to connect camera service");
            openCamera();
            return;
        }

        Camera.Parameters param = mCamera.getParameters();
        //  List<int[]> fpsRange = param.getSupportedPreviewFpsRange();
        param.setPreviewFpsRange(30000, 30000);
        param.setPreviewFormat(ImageFormat.NV21);
        // List<Camera.Size> list = param.getSupportedPreviewSizes();
        param.setPictureSize(1920, 1080);
//            param.getPreviewFpsRange();
        // 测试背光参数
//            int maxExposureCompensation = param.getMaxExposureCompensation();
//            int minExposureCompensation = param.getMinExposureCompensation();
//            Log.e(TAG, "maxExposureCompensation=" + maxExposureCompensation + "::minExposureCompensation=" + minExposureCompensation);
//            boolean isAutoExposure = param.isAutoExposureLockSupported();
//            boolean isAtuoWB = param.isAutoWhiteBalanceLockSupported();
//            Log.e(TAG, "isAE=" + isAutoExposure);
//            Log.e(TAG, "isAutoWB=" + isAtuoWB);
        // 设置背光参数
        // param.setExposureCompensation(3);
        //param.setAutoExposureLock(true);
//            param.setAutoExposureLock(true);
//            param.setSceneMode(SCENE_MODE_PARTY);
        mCamera.setParameters(param);
        mCamera.setDisplayOrientation(90);
        if (onCameraInitListener != null) {
            // faceActivity
            onCameraInitListener.initSuccess(mCamera);
        } else {
            // other circumstances take photo
//            try {
//                mCamera.setPreviewDisplay(mHolder);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mCamera.startPreview();
//            mCamera.takePicture(null, null, instance);
        }
    }


    /**
     * 释放相机
     */
    public synchronized void releaseCamera() {
        if (mHolder != null) {
            mHolder.removeCallback(null);
            mHolder.getSurface().release();
            mHolder = null;
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        synchronized (instance) {

        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (onCameraInitListener != null)
            onCameraInitListener.onPreviewCallBack(data);
        camera.addCallbackBuffer(data);
    }
}
