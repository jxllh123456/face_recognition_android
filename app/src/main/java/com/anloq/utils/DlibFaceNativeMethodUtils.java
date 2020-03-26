package com.anloq.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;

public class DlibFaceNativeMethodUtils {

    private static final String TAG = "DlibUtils";

    private OpenDoorInterface openDoorInterface;

    private DlibFaceNativeMethodUtils() {
    }

    public void setOpenDoorInterface(OpenDoorInterface openDoorInterface) {
        this.openDoorInterface = openDoorInterface;
    }

    private static DlibFaceNativeMethodUtils dlibFaceNativeMethodUtils;

    public synchronized static DlibFaceNativeMethodUtils getDlibFaceNativeMethodUtils() {

        if (dlibFaceNativeMethodUtils == null)
            return new DlibFaceNativeMethodUtils();
        else {
            return dlibFaceNativeMethodUtils;
        }
    }

    public synchronized native void RecoFromRect(Bitmap bitmap, int left, int top, int right, int bottom);

    public synchronized native void loadFaceFromRect(Bitmap bitmap, int userId, int left, int top, int right, int bottom);

    public synchronized native void initThresholdValue(float thresholdvalue);

    /**
     * call from native
     *
     * @param userid    ： 用户id
     * @param offset    ： 实际的偏差值
     * @param threshold ： 人脸识别阈值
     */
    public void openDoor(int userid, float offset, float threshold) {
        Log.e(TAG, "offset=" + offset);
        Log.e(TAG, "threshold=" + threshold);
        openDoorInterface.openDoor(userid);
    }

    static {
        System.loadLibrary("facedlib");
    }

}
