package com.anloq.bean;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class FaceQ2 {
    private Bitmap bitmap;
    private RectF rectF;

    public FaceQ2(Bitmap bitmap, RectF rectF) {
        this.bitmap = bitmap;
        this.rectF = rectF;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public RectF getRectF() {
        return rectF;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

}
