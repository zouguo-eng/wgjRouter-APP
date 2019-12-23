package com.example.samba;

import android.content.Context;
import android.content.Intent;

import com.example.bean.TaskInfo;
import com.example.bean.ThreadInfo;
import com.example.samba.thread.ThreadDAO;
import com.example.samba.thread.ThreadDAOImpl;
import com.example.utils.MyFileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MultiRemoteDownloadTask {
    private Context mContext;
    private TaskInfo taskInfo;
    private ThreadDAO threadDAO;
    private long mFinished = 0;
    public boolean isPause = false;
    private int mThreadCount;//分块下载线程数
    private List<DownloadThread> mThreadList = null;
    private long speedNotiSize = 10 * 1024 * 1024;//超过10M的更新时间慢
    private int slowNotiInternal = 1000;//慢时间间隔(大于speedNotiSize)
    private int fastNotiInternal = 100;//快更新时间

    public MultiRemoteDownloadTask(Context mContext, TaskInfo taskInfo, int mThreadCount) {
        this.mContext = mContext;
        this.taskInfo = taskInfo;
        this.mThreadCount = mThreadCount;
        threadDAO = new ThreadDAOImpl();
    }

    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadList = threadDAO.getThreadInfos(taskInfo.getFileUrl());

        if(threadList.size() == 0){
            //获取每个线程下载长度
            long length = taskInfo.getFileLength() / mThreadCount;
            for(int i = 0;i < mThreadCount;i++){
                //创建线程信息
                ThreadInfo threadInfo = new ThreadInfo();
                threadInfo.setId(i);
                threadInfo.setUrl(taskInfo.getFileUrl());
                threadInfo.setStart(i * length);
                threadInfo.setEnd((i + 1) * length - 1);
                threadInfo.setFinished(0);

                if(i == mThreadCount - 1){
                    threadInfo.setEnd(taskInfo.getFileLength());
                }

                //添加到线程集合
                threadList.add(threadInfo);
                //向数据库插入线程信息
                threadDAO.insertThread(threadInfo);
            }
        }

        mThreadList = new ArrayList<>();
        //启动多线程分块下载
        for(ThreadInfo threadInfo : threadList){
            DownloadThread downloadThread = new DownloadThread(threadInfo);
            MultiDownloadTask.mExecutorService.execute(downloadThread);//加入线程池
            mThreadList.add(downloadThread);
        }
    }

    /**
     * 判断是否所有线程执行完毕
     */
    private synchronized void checkAllThreadsFinished(){
        boolean allFinished = true;

        for(DownloadThread thread : mThreadList){
            if(!thread.isFinished){
                allFinished = false;
                break;
            }
        }

        if(allFinished){
            //删除线程信息
            threadDAO.deleteThread(taskInfo.getFileUrl());

            //发送任务完成广播
            Intent intent = new Intent(MultiRemoteDownloadService.ACTION_FINISHED);
            intent.putExtra("downloadRemoteFinishFileinfo", taskInfo);
            mContext.sendBroadcast(intent);
        }
    }

    class DownloadThread extends Thread{
        private ThreadInfo mThreadInfo;
        public boolean isFinished = false;//标记线程是否结束

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            final long start = mThreadInfo.getStart() + mThreadInfo.getFinished();

            OkHttpClient ohClient = new OkHttpClient();
            Request req = new Request.Builder()
                    .addHeader("Range","bytes=" + start + "-" + mThreadInfo.getEnd())
                    .method("GET",null)
                    .url(mThreadInfo.getUrl())
                    .build();
            Call call = ohClient.newCall(req);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    InputStream is;
                    RandomAccessFile raf;
                    BufferedOutputStream bos;
                    try {
                        is = response.body().byteStream();

                        //设置文件写入位置
                        File file = new File(MultiDownloadService.DOWNLOAD_PATH, taskInfo.getFileName());
                        raf = new RandomAccessFile(file,"rwd");
                        raf.seek(start);
                        bos = new BufferedOutputStream(new FileOutputStream(file));

                        mFinished += mThreadInfo.getFinished();

                        byte[] buffer = new byte[8 * 1024];
                        int bufferzLen = -1;
                        long beginTime = System.currentTimeMillis();
                        long notiTime = System.currentTimeMillis();

                        Intent intent = new Intent(MultiRemoteDownloadService.ACTION_UPDATE);

                        int i = 0;
                        while ((bufferzLen = is.read(buffer)) != -1){
                            bos.write(buffer,0,bufferzLen);
                            bos.flush();

                            //累加整个文件的完成进度
                            mFinished += bufferzLen;
                            //累加每个线程完成的进度
                            mThreadInfo.setFinished(mThreadInfo.getFinished() + bufferzLen);

                            i++;

                            long notifiedTime = System.currentTimeMillis() - notiTime;
                            long consumeTime = System.currentTimeMillis() - beginTime;
                            if(taskInfo.getFileLength() > speedNotiSize){
                                //文件大的刷新慢
                                if(notifiedTime > slowNotiInternal){
                                    notiTime = System.currentTimeMillis();

                                    long speed = i * bufferzLen;

                                    intent.putExtra("index", taskInfo.getIndexs());
                                    intent.putExtra("downloadProgress",(int)(mFinished * 1.0f / taskInfo.getFileLength() * 100));
                                    intent.putExtra("downloadSpeed", MyFileUtils.getInstance().formatTranSpeed(speed,2));
                                    intent.putExtra("downloadFinished",mFinished);
                                    intent.putExtra("downloadFileLength",taskInfo.getFileLength());
                                    intent.putExtra("downloadUseTime",consumeTime);
                                    mContext.sendBroadcast(intent);

                                    taskInfo.setTranTime(consumeTime);

                                    i = 0;
                                }
                            }else{
                                //文件比较小,刷新要快
                                if(notifiedTime > fastNotiInternal){
                                    notiTime = System.currentTimeMillis();

                                    long speed = i * bufferzLen;

                                    intent.putExtra("index", taskInfo.getIndexs());
                                    intent.putExtra("downloadProgress",(int)(mFinished * 1.0f / taskInfo.getFileLength() * 100));
                                    intent.putExtra("downloadSpeed", MyFileUtils.getInstance().formatTranSpeed(speed,2));
                                    intent.putExtra("downloadFinished",mFinished);
                                    intent.putExtra("downloadFileLength",taskInfo.getFileLength());
                                    intent.putExtra("downloadUseTime",consumeTime);
                                    mContext.sendBroadcast(intent);

                                    taskInfo.setTranTime(consumeTime);

                                    i = 0;
                                }
                            }

                            //下载暂停，保存进度
                            if(isPause){
                                threadDAO.updateThread(mThreadInfo.getId(),mThreadInfo.getUrl(),mThreadInfo.getFinished());
                                return;
                            }
                        }

                        //标识线程执行完毕
                        isFinished = true;
                        //检查下载任务是否完成
                        checkAllThreadsFinished();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }
            });
        }
    }
}
