package com.anloq.copydatfile.presenterimpl;

import com.anloq.copydatfile.listener.OnFileInitializeListener;
import com.anloq.copydatfile.model.IinitializeDatFileModel;
import com.anloq.copydatfile.modelimpl.InitializeDatFileModelImpl;
import com.anloq.copydatfile.presenter.IinitializeDatFilePresenter;
import com.anloq.copydatfile.view.IinitrializeDatFileView;

public class InitializeDatFilePresenterImpl implements IinitializeDatFilePresenter {

    private IinitializeDatFileModel mInitializeModel;
    private IinitrializeDatFileView mInitializeView;


    public InitializeDatFilePresenterImpl(IinitrializeDatFileView view) {
        this.mInitializeView = view;
        mInitializeModel = new InitializeDatFileModelImpl();
    }

    @Override
    public void initializeModelFile() {
        mInitializeModel.initializeDatFile(new OnFileInitializeListener() {
            @Override
            public void onFileInitialize(boolean isSuccess) {
                mInitializeView.initializeDatFile(isSuccess);
            }
        });
    }
}
