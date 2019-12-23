package com.example.samba.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MyIntentServiceDownload extends IntentService {
    public static final String DOWNLOAD_URL = "download_url";
    public static final String INDEX_FLAG = "index_flag";
    public static UpdateUI updateUI;

    public static void setUpdateUI(UpdateUI updateUIInterface){
        updateUI = updateUIInterface;
    }

    public MyIntentServiceDownload() {
        super("MyIntentServiceDownload");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("zouguo","Thread id is " + Thread.currentThread().getId());

        boolean isDownOk = downloadFile(intent.getStringExtra(DOWNLOAD_URL));
        Message msg = new Message();
        msg.what = intent.getIntExtra(INDEX_FLAG,0);
        msg.obj = isDownOk;
        if(updateUI != null){
            updateUI.updateUI(msg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("zouguo","onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("zouguo","onStart " + startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("zouguo","onStartCommand " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("zouguo","onBind");
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("zouguo","onDestroy IntentService");
    }

    public interface UpdateUI{
        void updateUI(Message message);
    }

    //下载具体实现
    private boolean downloadFile(String downloadUrl){
        boolean isOk = false;


        return isOk;
    }
}
