package com.anloq.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anloq.bean.FaceQ2;
import com.anloq.copydatfile.presenter.IinitializeDatFilePresenter;
import com.anloq.copydatfile.presenterimpl.InitializeDatFilePresenterImpl;
import com.anloq.copydatfile.view.IinitrializeDatFileView;
import com.anloq.runnable.CopyFileCallable;
import com.anloq.runnable.Q2ConsumerRunnable;
import com.anloq.utils.CustomCamera;
import com.anloq.utils.DlibFaceNativeMethodUtils;
import com.anloq.utils.MyMediaManager;
import com.anloq.utils.OpenDoorInterface;
import com.anloq.utils.Preview;
import com.example.summer.facedemo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class AdActivity1 extends AppCompatActivity implements CustomCamera.OnCameraInitListener, OpenDoorInterface, Q2ConsumerRunnable.Unknown, IinitrializeDatFileView {
    private static final String TAG = "AdActivity1";
    private static final int Q2_CAPACITY = 10;
    private static final int FIRST_CONSUMER_THREAD_POOL_SIZE = 1;

    // 要申请的权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
//    /**
//     * 各指示灯和继电器的驱动设备节点
//     */
//    private static final String LED_CTL_PATH = "/sys/class/zh_gpio_out/out";
//
//    //关灯
//    private static final String[] LED_OFF_VAL = {
//            "2", //红灯
//            "4", //绿灯
//            "6", //摄像头白灯
//            "8", //摄像头红灯
//            "12" //工作指示灯
//    };
//    //开灯
//    private static final String[] LED_ON_VAL = {
//            "1", //红灯
//            "3", //绿灯
//            "5", //摄像头白灯
//            "7", //摄像头红灯
//            "11" //工作指示灯
//    };
//    //继电器
//    private static final String RELAY_ON = "9";
//    private static final String RELAY_OFF = "10";


    private CustomCamera customCamera;
    private FrameLayout frameLayout;
    private Preview mPreview;
    private Paint mPaint = new Paint();
    private SurfaceView surfaceViewDraw;
    private static ThreadPoolExecutor mThreadPool;
    // private ThreadPoolExecutor mThreadPoolCompareBmpDiff;
    Canvas canvasDraw;
    DlibFaceNativeMethodUtils utils;
    private int width;
    private int height;

//    private Button btnRed;
//    private Button btnGreen;
//    private Button btnWhite;
//    private Button btnJidianqi;
    private TextView tvName;

    private boolean white;
    private boolean red;
    private boolean green;
    private boolean jidianqi;


    private LinkedBlockingQueue<FaceQ2> q2;
    private Q2ConsumerRunnable mConsumerRunnable;
    private HashMap<Integer, String> mHashMap;
    private IinitializeDatFilePresenter mInitrilizeDatFilePresenter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    static {
        System.loadLibrary("facedlib");
    }


    private void loadFace(Bitmap bitmap, int userId) {
        Bitmap bmp_565 = bitmap.copy(Bitmap.Config.RGB_565, true);
        FaceDetector detector = new FaceDetector(bmp_565.getWidth(), bmp_565.getHeight(), 1);
        FaceDetector.Face[] faceList = new FaceDetector.Face[1];
        detector.findFaces(bmp_565, faceList);
        for (FaceDetector.Face face : faceList) {
            if (face == null) {
                continue;
            }
            PointF pf = new PointF();
            face.getMidPoint(pf);
            RectF r = new RectF();
            float dis = face.eyesDistance();
            r.left = pf.x - dis;
            // Log.e("async","up left=" + r.left);
            r.right = pf.x + dis;
            r.top = pf.y - dis + dis / 2;
            r.bottom = pf.y + dis + dis / 2;
            utils.loadFaceFromRect(bitmap, userId, (int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
            Toast.makeText(this, "人脸下发成功.", Toast.LENGTH_SHORT).show();
        }
        bmp_565.recycle();
        bitmap.recycle();
    }

//    private void loadFace(Bitmap bitmap int userid)
//    {
//        Bitmap bmp_565 = bitmap.copy(Bitmap.Config.RGB_565, true);
//        FaceDetector detector = new FaceDetector(bmp_565.getWidth(), bmp_565.getHeight(), 1);
//        FaceDetector.Face[] faceList = new FaceDetector.Face[1];
//        detector.findFaces(bmp_565, faceList);
//        for (FaceDetector.Face face : faceList) {
//            if (face == null) {
//                continue;
//            }
//            PointF pf = new PointF();
//            face.getMidPoint(pf);
//            RectF r = new RectF();
//            float dis = face.eyesDistance();
//            r.left = pf.x - dis;
//            // Log.e("async","up left=" + r.left);
//            r.right = pf.x + dis;
//            r.top = pf.y - dis + dis / 2;
//            r.bottom = pf.y + dis + dis / 2;
//            utils.loadFaceFromRect(bitmap, userid, (int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
//            Toast.makeText(this, "人脸下发成功.", Toast.LENGTH_SHORT).show();
//        }
//        bmp_565.recycle();
//        bitmap.recycle();
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad1);
        frameLayout = (FrameLayout) findViewById(R.id.fl_container);
        surfaceViewDraw = (SurfaceView) findViewById(R.id.surface_draw_ad1);

//        btnRed = findViewById(R.id.btn_red);
//        btnGreen = findViewById(R.id.btn_green);
//        btnWhite = findViewById(R.id.btn_white);
//        btnJidianqi = findViewById(R.id.btn_jidianqi);
        tvName = findViewById(R.id.tv_name);

//        btnRed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!red) {
//                    red = true;
//                    ledAndSwitch(LED_ON_VAL[0]);
//                } else {
//                    ledAndSwitch(LED_OFF_VAL[0]);
//                    red = false;
//                }
//            }
//        });
//
//        btnGreen.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!green) {
//                    green = true;
//                    ledAndSwitch(LED_ON_VAL[1]);
//                } else {
//                    green = false;
//                    ledAndSwitch(LED_OFF_VAL[1]);
//                }
//            }
//        });
//
//        btnWhite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!white) {
//                    ledAndSwitch(LED_ON_VAL[2]);
//                    white = true;
//                } else {
//                    ledAndSwitch(LED_OFF_VAL[2]);
//                    white = false;
//                }
//            }
//        });
//
//        btnJidianqi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!jidianqi) {
//                    jidianqi = true;
//                    ledAndSwitch(RELAY_ON);
//                } else {
//                    jidianqi = false;
//                    ledAndSwitch(RELAY_OFF);
//                }
//            }
//        });
        // 权限的检查及申请
        int p_storage = ContextCompat.checkSelfPermission(this,permissions[0]);
        int p_camera = ContextCompat.checkSelfPermission(this,permissions[1]);
        if (p_storage!= PackageManager.PERMISSION_GRANTED || p_camera!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, 321);
        }
//        createFile();
        mInitrilizeDatFilePresenter = new InitializeDatFilePresenterImpl(this);
        mInitrilizeDatFilePresenter.initializeModelFile();
    }




    private void createFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "test0");
        if (!file.exists()) {
            boolean isSuccess = file.mkdirs();
            Log.e(TAG, "createFile="+isSuccess);
        } else {
            Toast.makeText(this, "文件夹已存在", Toast.LENGTH_LONG).show();
        }
    }


    private void initData() {
        // FixedThreadPool : core threads and maximum threads equally.
        mThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(FIRST_CONSUMER_THREAD_POOL_SIZE);
        // mThreadPoolCompareBmpDiff = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        customCamera = CustomCamera.getInstance();
        customCamera.setOnCameraInitListener(this);
        customCamera.initCamera();

        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3f);
        mPaint.setStyle(Paint.Style.STROKE);

        surfaceViewDraw.setZOrderOnTop(true);
        surfaceViewDraw.getHolder().setFormat(PixelFormat.TRANSLUCENT);

    }


    @Override
    public void initSuccess(Camera camera) {
        runOnUiThread(() -> {
            mPreview = new Preview(AdActivity1.this, camera);
            if (customCamera != null)
                customCamera.setPreviewCallback();
            frameLayout.addView(mPreview);
        });
        // 初始化Q2和第二层的消费Runnable
        q2 = new LinkedBlockingQueue<>(Q2_CAPACITY);
        ExecutorService singleThreadQ2Customer = Executors.newSingleThreadExecutor();
        mConsumerRunnable = new Q2ConsumerRunnable(q2, utils, this);
        singleThreadQ2Customer.execute(mConsumerRunnable);

    }

    // 用作求余，跳帧
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    List<Float> row_gray_list_first = new ArrayList<>();

    @Override
    public void onPreviewCallBack(byte[] data) {
        try {
            // 这里的Runnable是第一层的消费Runnable同时作为第二层的生产Runnable
            mThreadPool.execute(() -> {
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, 1920, 1080, null);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, 1920, 1080), 90, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                Bitmap rotatedBitmap = rotateBitmap(bmp, 270);
                // saveBitmap(rotatedBitmap);
                Bitmap bmp_565 = rotatedBitmap.copy(Bitmap.Config.RGB_565, true);
                FaceDetector detector = new FaceDetector(bmp_565.getWidth(), bmp_565.getHeight(), 1);
                FaceDetector.Face[] faceList = new FaceDetector.Face[1];
                detector.findFaces(bmp_565, faceList);
                // faceList目前长度只为1
                for (FaceDetector.Face face : faceList) {
                    if (face == null || face.confidence() <= 0.51) {
                        break;
                    }
                    PointF pf = new PointF();
                    face.getMidPoint(pf);
                    RectF r = new RectF();
                    float dis = face.eyesDistance();
                    r.left = pf.x - dis;
                    r.right = pf.x + dis;
                    r.top = pf.y - dis + dis / 2;
                    r.bottom = pf.y + dis + dis / 2;
                    // 在这里将FaceQ2 (Bitmap,RectF ) 加入 Q2 (LinkedBlockingQueue)
                    // 跳帧，3帧入队一帧，这里不能直接将bmp赋值进去，这样仅仅是将bmp的引用赋值了过去，而真正的复制像素值需要调用bitmap的copy方法
                    int mod = atomicInteger.getAndIncrement() % 5;
                    if (mod == 0 || mod == 1) {
                        q2.offer(new FaceQ2(rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true), r));
                    }
                    drawFaceRectSK((int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
                }
                bmp_565.recycle();
            });
        } catch (RejectedExecutionException exception) {
            Log.e(TAG, exception.toString());
        }
    }


    private void drawFaceRectSK(int left, int top, int right, int bottom) {

//        top = (int) (top*0.8f);
//        bottom = (int) (bottom*0.8f);
//        left = left*3;
//        right = (int) (right*2.2f);
//        left = left + 700;
//        right = right + 670;
//        top = top - 80;
//        bottom = bottom - 100;

        try {
            canvasDraw = surfaceViewDraw.getHolder().lockCanvas();
            if (canvasDraw == null) return;
            canvasDraw.drawColor(0, PorterDuff.Mode.CLEAR);
            mPaint.setColor(Color.rgb(0, 188, 255));
        } catch (Exception e) {
            e.toString();
        }
        Path drawPath = new Path();
        //人脸框大概宽度
        int faceWidth = right - left;
        int lineLen = (int) (faceWidth * 0.15f);//人脸框边角长度
        float left_top_y1, left_top_x3, right_top_x1, right_top_y3, right_bottom_y1, right_bottom_x3, left_bottom_x1, left_bottom_y3, left_top_x, left_top_y, right_top_x, right_top_y, right_bottom_x, right_bottom_y, left_bottom_x, left_bottom_y;


        left_top_x = left;
        left_top_y = top;
        right_top_x = right;
        right_top_y = top;
        right_bottom_x = right;
        right_bottom_y = bottom;
        left_bottom_x = left;
        left_bottom_y = bottom;


//        left_top_x = left_top_x * 2.05f;
//        right_top_x = right_top_x * 1.95f;
//        right_bottom_x = right_bottom_x * 1.95f;
//        left_bottom_x = left_bottom_x * 2.05f;
//
//        left_top_y = left_top_y * 1.35f;
//        right_top_y = right_top_y * 1.35f;
//
//        left_bottom_y = left_bottom_y * 1.65f;
//        right_bottom_y = right_bottom_y * 1.65f;


        left_top_y1 = left_top_y + lineLen;
        left_top_x3 = left_top_x + lineLen;

        right_top_x1 = right_top_x - lineLen;
        right_top_y3 = right_top_y + lineLen;

        right_bottom_y1 = right_bottom_y - lineLen;
        right_bottom_x3 = right_bottom_x - lineLen;

        left_bottom_x1 = left_bottom_x + lineLen;
        left_bottom_y3 = left_bottom_y - lineLen;


        drawPath.moveTo(left_top_x, left_top_y1);
        drawPath.lineTo(left_top_x, left_top_y);
        drawPath.lineTo(left_top_x3, left_top_y);

        drawPath.moveTo(right_top_x1, right_top_y);
        drawPath.lineTo(right_top_x, right_top_y);
        drawPath.lineTo(right_top_x, right_top_y3);


        drawPath.moveTo(right_bottom_x, right_bottom_y1);
        drawPath.lineTo(right_bottom_x, right_bottom_y);
        drawPath.lineTo(right_bottom_x3, right_bottom_y);

        drawPath.moveTo(left_bottom_x1, left_bottom_y);
        drawPath.lineTo(left_bottom_x, left_bottom_y);
        drawPath.lineTo(left_bottom_x, left_bottom_y3);

        try {
            canvasDraw.drawPath(drawPath, mPaint);
            surfaceViewDraw.getHolder().unlockCanvasAndPost(canvasDraw);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//     public void openDoor(int userid) {
//        Log.e(TAG, "userid from native =" + userid);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    canvasDraw = surfaceViewDraw.getHolder().lockCanvas();
//                    canvasDraw.drawColor(0, PorterDuff.Mode.CLEAR);
//                    surfaceViewDraw.getHolder().unlockCanvasAndPost(canvasDraw);
//                } catch (Exception e) {
//                    e.toString();
//                }
//            }
//        });
//    }


    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }


    @Override
    public void unknow() {
        MyMediaManager.getInstance().playAudio(R.raw.unknown);
        // 此时提示'未识别'
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvName.setText("未识别");
                try {
                    canvasDraw = surfaceViewDraw.getHolder().lockCanvas();
                    canvasDraw.drawColor(0, PorterDuff.Mode.CLEAR);
                    surfaceViewDraw.getHolder().unlockCanvasAndPost(canvasDraw);
                } catch (Exception e) {
                    e.toString();
                }
            }
        });
        try {
            FaceQ2 faceQ2 = q2.take();
            Bitmap bitmap = faceQ2.getBitmap();
            long time = System.currentTimeMillis();
            saveBitmap("unknow_" + time, bitmap, faceQ2.getRectF());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        q2.clear();
        mConsumerRunnable.setCount();
        atomicInteger.set(0);
        mConsumerRunnable.setPause(false);
    }


    @Override
    public void openDoor(int userid) {
        mConsumerRunnable.setPause(true);
        try {
            FaceQ2 faceQ2 = q2.take();
            Bitmap bitmap = faceQ2.getBitmap();
            long time = System.currentTimeMillis();
            saveBitmap(userid + "_" + time, bitmap, faceQ2.getRectF());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MyMediaManager.getInstance().playAudio(R.raw.known);
        Log.e(TAG, "userid from native =" + userid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    canvasDraw = surfaceViewDraw.getHolder().lockCanvas();
                    canvasDraw.drawColor(0, PorterDuff.Mode.CLEAR);
                    surfaceViewDraw.getHolder().unlockCanvasAndPost(canvasDraw);
                    tvName.setText("已识别:" + mHashMap.get(userid));
                } catch (Exception e) {
                    e.toString();
                }
            }
        });
        // resetConsumerRunnable.
        q2.clear();
        atomicInteger.set(0);
        mConsumerRunnable.setCount();
        mConsumerRunnable.setPause(false);
    }


//    private void ledAndSwitch(String val) {
//        File file = new File(LED_CTL_PATH);
//        if (!file.exists() || !file.canWrite()) {
//            return;
//        }
//        try {
//            FileOutputStream fout = new FileOutputStream(file);
//            PrintWriter pWriter = new PrintWriter(fout);
//            pWriter.println(val);
//            pWriter.flush();
//            pWriter.close();
//            fout.close();
//        } catch (IOException re) {
//        }
//    }


    public Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }


    private void saveBitmap(Bitmap bitmap){

        try {
            FileOutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator  + "temp.jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param fileName : 由时间+姓名(或 unknow)组成
     * @param bitmap
     * @param r
     */
    private void saveBitmap(String fileName, Bitmap bitmap, RectF r) {
        try {
//            File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName+".jpg");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
            FileOutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + fileName + ".jpg");
            Bitmap extractBitmap = Bitmap.createBitmap(bitmap, (int) r.left, (int) r.top, (int) (r.right - r.left), (int) (r.bottom - r.top));
            extractBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeDatFile(boolean isSuccess) {
        if (!isSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AdActivity1.this, "模型文件拷贝失败", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        utils = DlibFaceNativeMethodUtils.getDlibFaceNativeMethodUtils();
        utils.setOpenDoorInterface(this);
        utils.initThresholdValue(0.42f);
        try {
            FileInputStream fis0 = new FileInputStream("/sdcard/lilinhai.jpg");
            Bitmap bitmap0 = BitmapFactory.decodeStream(fis0);
            loadFace(bitmap0, 10001);
            fis0.close();
            bitmap0.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHashMap = new HashMap<>();
        mHashMap.put(10001, "李林海(10001)");
        mHashMap.put(10002, "郭宁(10002)");
        mHashMap.put(10003, "黄文辉(10003)");
        mHashMap.put(10004, "李海毅(10004)");
        initData();
    }


//    private Bitmap loadBitmap(String fileName) {
//        InputStream inputStream = null;
//        try {
//            inputStream = getAssets().open(fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return BitmapFactory.decodeStream(inputStream);
//    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //如果没有获取权限，那么可以提示用户去设置界面--->应用权限开启权限
                } else {
                    //获取权限成功提示，可以不要
                    Toast toast = Toast.makeText(this, "获取权限成功", Toast.LENGTH_LONG);
                }
        }
    }

}
