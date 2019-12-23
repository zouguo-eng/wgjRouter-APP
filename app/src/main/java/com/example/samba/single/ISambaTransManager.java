package com.example.samba.single;

import java.io.File;

public interface ISambaTransManager {
    //文件下载
    void download(String url, File localFile,INetDownloadCallBack callBack);
    //文件上传
    void upload(File file,String remoteUrl,INetUploadCallBack callBack);
}
