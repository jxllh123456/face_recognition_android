package com.anloq.copydatfile.presenter;

public interface IinitializeDatFilePresenter {
    /**
     * 先判断是否存在文件夹，再判断是否拷贝成功，最终给view只返回一个boolean
     */
    void initializeModelFile();
}
