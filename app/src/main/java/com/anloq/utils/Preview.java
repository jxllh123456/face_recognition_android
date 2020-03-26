package com.anloq.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by summer on 5/16/18.
 */

public class Preview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private static final String TAG = "preview";

    public SurfaceHolder getmHolder() {
        return mHolder;
    }

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public Preview(Context context, Camera camera) {
        super(context);

        this.mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mCamera.autoFocus(this);
        } catch (Exception e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCamera.autoFocus(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.e(TAG, "autofocus=" + success);
        if (success){
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            mCamera.setParameters(parameters);
        }
    }
}
