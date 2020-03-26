package com.anloq.runnable;

import android.os.Environment;

import com.anloq.application.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.Callable;

public class CopyFileCallable implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {


        InputStream inputStream = MyApplication.getmContext().getAssets().open("a.dat");
        FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "face","a.dat"));
        byte[] buffer = new byte[1024];
        int byteCount;
        while ((byteCount = inputStream.read(buffer)) != -1) {//循环从输入流读取 buffer字节
            fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
        }
        //fos.flush();//刷新缓冲区
        inputStream.close();
        fos.close();

        InputStream inputStream1 = MyApplication.getmContext().getAssets().open("b.dat");
        FileOutputStream fos1 = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "face","b.dat"));
        byte[] buffer1 = new byte[1024];
        int byteCount1;
        while ((byteCount1 = inputStream1.read(buffer1)) != -1) {//循环从输入流读取 buffer字节
            fos1.write(buffer1, 0, byteCount1);//将读取的输入流写入到输出流
        }
        // fos1.flush();//刷新缓冲区
        inputStream1.close();
        fos1.close();

        return true;
    }

}
