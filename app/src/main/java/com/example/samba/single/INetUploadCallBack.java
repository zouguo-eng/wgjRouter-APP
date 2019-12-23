package com.example.samba.single;

public interface INetUploadCallBack {
    //成功回调
    void success(String remoteUrl,int useTime);
    //进度回调
    void progress(int progress);
    //失败回调
    void failed(Throwable throwable);
}
