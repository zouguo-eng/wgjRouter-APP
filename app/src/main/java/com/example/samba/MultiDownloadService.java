package com.example.samba;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.bean.TaskInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

import jcifs.smb.SmbFile;

public class MultiDownloadService extends Service {
    public static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/com.example.wgjrouter/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_STOP_ALL = "ACTION_STOP_ALL";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final int MSG_INIT = 0;
    private InitThread mInitThread = null;
    private Map<Integer,MultiDownloadTask> mTasks = new LinkedHashMap<>();

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
            MultiDownloadTask task = mTasks.get(taskInfo.getIndexs());
            if(task != null){
                //停止下载任务
                task.isPause = true;
            }
        }else if(intent != null && intent.getAction().equals(ACTION_STOP_ALL)){
            Log.d("zouguo","全部暂停本地:" + mTasks.toString());
            for(int i = 0;i < mTasks.size();i++){
                MultiDownloadTask task = mTasks.get(i);
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
                    MultiDownloadTask mTask = new MultiDownloadTask(MultiDownloadService.this, taskInfo,1);
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
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                SmbFile smbFile = new SmbFile(taskInfo.getFileUrl());
                //获得文件长度
                long length = smbFile.getContentLength();
                //在本地创建文件
                File fileDir = new File(DOWNLOAD_PATH);
                if(!fileDir.exists()){
                    fileDir.mkdir();
                }
                File file = new File(fileDir, taskInfo.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                //设置本地文件长度
                raf.setLength(length);
                taskInfo.setFileLength(length);
                handler.obtainMessage(MSG_INIT, taskInfo).sendToTarget();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    if(raf != null){
                        raf.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
