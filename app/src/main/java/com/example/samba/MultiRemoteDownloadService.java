package com.example.samba;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.bean.TaskInfo;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MultiRemoteDownloadService extends Service {
    public static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/com.example.wgjrouter/";
    public static final String ACTION_START = "ACTION_START_REMOTE";
    public static final String ACTION_STOP = "ACTION_STOP_REMOTE";
    public static final String ACTION_START_ALL = "ACTION_START_REMOTE_ALL";
    public static final String ACTION_STOP_ALL = "ACTION_STOP_REMOTE_ALL";
    public static final String ACTION_UPDATE = "ACTION_UPDATE_REMOTE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED_REMOTE";
    public static final int MSG_INIT = 0;
    private InitThread mInitThread = null;
    private Map<Integer,MultiRemoteDownloadTask> mTasks = new LinkedHashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得Activity传递的参数
        if(intent != null && intent.getAction().equals(ACTION_START)){
            TaskInfo taskInfo = (TaskInfo) intent.getSerializableExtra("fileInfo");

            //启动初始化线程
            mInitThread = new InitThread(taskInfo);
            MultiDownloadTask.mExecutorService.execute(mInitThread);
        }else if(intent != null && intent.getAction().equals(ACTION_STOP)){
            TaskInfo taskInfo = (TaskInfo) intent.getSerializableExtra("fileInfo");
            //从下载集合中取出下载任务
            MultiRemoteDownloadTask task = mTasks.get(taskInfo.getIndexs());
            if(task != null){
                //停止下载任务
                task.isPause = true;
            }
        }else if(intent != null && intent.getAction().equals(ACTION_STOP_ALL)){
            Log.d("zouguo","全部暂停远程:" + mTasks.toString());
            for(int i = 0;i < mTasks.size();i++){
                MultiRemoteDownloadTask task = mTasks.get(i);
                if(task != null){
                    //停止任务
                    task.isPause = true;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_INIT:
                    TaskInfo taskInfo = (TaskInfo) msg.obj;

                    //启动下载任务
                    MultiRemoteDownloadTask mTask = new MultiRemoteDownloadTask(MultiRemoteDownloadService.this, taskInfo,1);
                    mTask.download();
                    //把下载任务加载到集合
                    mTasks.put(taskInfo.getIndexs(),mTask);
                    break;
            }
        }
    };

    class InitThread extends Thread{
        private TaskInfo taskInfo;

        public InitThread(TaskInfo taskInfo) {
            this.taskInfo = taskInfo;
        }

        @Override
        public void run() {
            //连接网络文件
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(taskInfo.getFileUrl())
                    .method("GET", null)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    //获得文件长度
                    long length = response.body().contentLength();
                    //在本地创建文件
                    File fileDir = new File(DOWNLOAD_PATH);
                    if(!fileDir.exists()){
                        fileDir.mkdir();
                    }
                    File file = new File(fileDir, taskInfo.getFileName());
                    RandomAccessFile raf = new RandomAccessFile(file,"rwd");
                    //设置本地文件长度
                    raf.setLength(length);
                    taskInfo.setFileLength(length);
                    handler.obtainMessage(MSG_INIT, taskInfo).sendToTarget();

                    try {
                        raf.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}