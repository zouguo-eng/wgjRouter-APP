package com.example.samba.single;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class JcifsSambaTransManager implements ISambaTransManager {
    private static Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void download(String url, File targetDir, final INetDownloadCallBack callBack) {
        long beginTime = System.currentTimeMillis();

        SmbFileInputStream sfis = null;
        BufferedOutputStream bos = null;

        try {
            try {
                SmbFile smbDown = new SmbFile(url);
                final File localFile = new File(targetDir + "/" + smbDown.getName());
                sfis = new SmbFileInputStream(smbDown);
                bos = new BufferedOutputStream(new FileOutputStream(localFile));

                byte[] bt = new byte[8 * 1024];
                final long totalLen = smbDown.length();
                long curLen = 0;

                int bufferzLen = 0;

                while((bufferzLen = sfis.read(bt)) != -1){
                    bos.write(bt,0,bufferzLen);
                    bos.flush();
                    curLen += bufferzLen;

                    final long finalCurlen = curLen;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.progress((int)(finalCurlen * 1.0f / totalLen * 100));
                        }
                    });
                }

                final long useTime = System.currentTimeMillis() - beginTime;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.success(localFile,useTime);
                    }
                });
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            } catch (final SmbException e) {
                e.printStackTrace();
            } catch (final UnknownHostException e) {
                e.printStackTrace();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e){
                e.printStackTrace();
            }
        } catch (final Throwable e) {
            e.printStackTrace();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.failed(e);
                }
            });
        } finally {
            try {
                if(sfis != null)
                    sfis.close();
                if(bos != null)
                    bos.close();
            } catch (final IOException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.failed(e);
                    }
                });
            }
        }
    }

    @Override
    public void upload(File file, String targetUrl, INetUploadCallBack callBack) {

    }
}
