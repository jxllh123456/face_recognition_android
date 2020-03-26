package com.anloq.runnable;

import android.graphics.RectF;
import android.util.Log;

import com.anloq.bean.FaceQ2;
import com.anloq.utils.DlibFaceNativeMethodUtils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Q2ConsumerRunnable implements Runnable {
    private static final String TAG = "Q2Consumer";
    private LinkedBlockingQueue<FaceQ2> q2;
    private DlibFaceNativeMethodUtils utils;
    private Unknown unknown;
    private AtomicInteger count = new AtomicInteger(5);
    private volatile boolean isPause = false;

    public Q2ConsumerRunnable(LinkedBlockingQueue<FaceQ2> q2, DlibFaceNativeMethodUtils utils, Unknown unknown) {
        this.q2 = q2;
        this.utils = utils;
        this.unknown = unknown;
    }


    @Override
    public void run() {
        while (!isPause) {
            if (count.getAndDecrement() == 0) {
                setPause(true);
                // 未识别
                unknown.unknow();
            }
            try {
                FaceQ2 faceQ2 = q2.take();
                RectF r = faceQ2.getRectF();
                utils.RecoFromRect(faceQ2.getBitmap(), (int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setCount() {
        count.set(5);
    }

    public interface Unknown {
        void unknow();
    }
}
