package com.example.samba.single;

import java.io.File;

public interface INetDownloadCallBack {
    //成功回调
    void success(File localFile,long useTime);
    //进度回调
    void progress(int progress);
    //失败回调
    void failed(Throwable throwable);
}
