package com.anloq.copydatfile.modelimpl;

import android.os.Environment;
import android.util.Log;

import com.anloq.copydatfile.listener.OnFileInitializeListener;
import com.anloq.copydatfile.model.IinitializeDatFileModel;
import com.anloq.runnable.CopyFileCallable;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class InitializeDatFileModelImpl implements IinitializeDatFileModel {

    @Override
    public void initializeDatFile(OnFileInitializeListener onFileInitializeListener) {
        // 判断/sdcard/face 下是否存在.dat文件
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "face");
        if (!file.exists()) {
            boolean ismkdir = file.mkdirs();
            Log.e("copyfile", ismkdir + "");
            // 拷贝.dat文件
            boolean copyResult = copyFile();
            if (copyResult) {
                onFileInitializeListener.onFileInitialize(true);
            } else {
                onFileInitializeListener.onFileInitialize(false);
                file.delete();
            }
        } else {
            onFileInitializeListener.onFileInitialize(true);
        }
    }


    private boolean copyFile() {
        CopyFileCallable copyFileCallable = new CopyFileCallable();
        FutureTask<Boolean> futureTask = new FutureTask<>(copyFileCallable);
        new Thread(futureTask).start();
        // 获取线程运算后的结果
        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            Log.e("copyfile", e.toString());
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            Log.e("copyfile", e.toString());
            e.printStackTrace();
            return false;
        }
    }
}
