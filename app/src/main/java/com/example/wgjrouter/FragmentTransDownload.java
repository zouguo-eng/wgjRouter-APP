package com.example.wgjrouter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.adapter.MyRecTransAdapter;
import com.example.bean.TaskInfo;
import com.example.database.FileTrans;
import com.example.event.MessageEvent;
import com.example.samba.MultiDownloadService;
import com.example.samba.MultiRemoteDownloadService;
import com.example.samba.thread.TaskDAO;
import com.example.samba.thread.TaskDAOImpl;
import com.example.utils.MyFileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentTransDownload extends Fragment {
    private RecyclerView layout_trans_download_rv;
    List<TaskInfo> taskInfoList = null;
    MyRecTransAdapter myRecTransAdapter = null;

    private String eventFileName = null;
    private String eventFileUrl = null;
    //本地路径--需要
    private String localDirPath = null;
    private TaskDAO taskDAO = new TaskDAOImpl();//用于添加任务到数据库中(主要是用于退出应用重新回到界面时加载任务列表)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册接收下载任务广播
        EventBus.getDefault().register(this);
        //注册下载进度更新广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MultiDownloadService.ACTION_UPDATE);
        intentFilter.addAction(MultiDownloadService.ACTION_FINISHED);
        intentFilter.addAction(MultiRemoteDownloadService.ACTION_UPDATE);
        intentFilter.addAction(MultiRemoteDownloadService.ACTION_FINISHED);
        getActivity().registerReceiver(mReceive,intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fg_trans_download,null);

        initView(view);

        //从任务数据库中读取未完成的任务(完成的任务会从TaskInfo数据库中删除)
        getUnfinishedDownloadTaskFromDB();
        return view;
    }

    private void initView(View view){
        layout_trans_download_rv = view.findViewById(R.id.layout_trans_download_rv);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        layout_trans_download_rv.setLayoutManager(llm);

        taskInfoList = new ArrayList<>();
        myRecTransAdapter = new MyRecTransAdapter(getActivity(), taskInfoList);
        layout_trans_download_rv.setAdapter(myRecTransAdapter);

        myRecTransAdapter.setOnItemListener(new MyRecTransAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent start = new Intent(getActivity(), MultiDownloadService.class);
                start.setAction(MultiDownloadService.ACTION_START);
                start.putExtra("fileInfo", taskInfoList.get(position));
                getActivity().startService(start);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void getUnfinishedDownloadTaskFromDB(){
        List<TaskInfo> taskInfos = taskDAO.getDownloadTaskInfos();

        Log.d("zouguo","任务数量:" + taskInfos.size());

        //任务都已完成，但界面可能未刷新
        taskInfoList.clear();
        myRecTransAdapter.notifyDataSetChanged();

        for(int i = 0;i < taskInfos.size();i++){
            //更新位置索引
            taskInfos.get(i).setIndexs(i);

            //未完成的下载任务重新加入任务列表
            taskInfoList.add(i,taskInfos.get(i));
            myRecTransAdapter.notifyItemInserted(i);

            //判断是远程下载还是本地下载
            switch (taskInfos.get(i).getUpOrDownOrRemote()){
                case 1:
                    Intent startLocal = new Intent(getActivity(), MultiDownloadService.class);
                    startLocal.setAction(MultiDownloadService.ACTION_START);
                    startLocal.putExtra("fileInfo", taskInfos.get(i));
                    getActivity().startService(startLocal);
                    break;
                case 2:
                    Intent startRemote = new Intent(getActivity(), MultiRemoteDownloadService.class);
                    startRemote.setAction(MultiRemoteDownloadService.ACTION_START);
                    startRemote.putExtra("fileInfo", taskInfos.get(i));
                    getActivity().startService(startRemote);
                    break;
            }
        }
    }

    BroadcastReceiver mReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MultiDownloadService.ACTION_UPDATE)){
                //条目更新进度条
                int id = intent.getIntExtra("index",0);
                int progress = intent.getIntExtra("downloadProgress",0);
                String speed = intent.getStringExtra("downloadSpeed");
                long finished = intent.getLongExtra("downloadFinished",0);
                long fileLength = intent.getLongExtra("downloadFileLength",0);
                long consumeTime = intent.getLongExtra("downloadUseTime",0);

                List payloadList = new ArrayList();
                payloadList.add("downloadProgress");
                payloadList.add(progress);
                payloadList.add(speed);
                payloadList.add(finished);
                payloadList.add(fileLength);
                payloadList.add(consumeTime);
                myRecTransAdapter.notifyItemChanged(id,payloadList);
            }else if(intent.getAction().equals(MultiDownloadService.ACTION_FINISHED)){
                //条目下载完成
                TaskInfo taskInfo = (TaskInfo) intent.getSerializableExtra("downloadFinishFileinfo");

                List payloadList = new ArrayList();
                payloadList.add("downloadOK");
                payloadList.add(100);
                payloadList.add("");
                payloadList.add(taskInfo.getTranFinished());
                payloadList.add(taskInfo.getFileLength());
                myRecTransAdapter.notifyItemChanged(taskInfo.getIndexs(),payloadList);

                //移除完成的条目
//                removeItem(taskInfo);

                //删除任务信息
                taskDAO.deleteTask(taskInfo.getFileUrl());
                //添加下载完成记录
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
                FileTrans fileTran = new FileTrans(0, MyFileUtils.getInstance().checkFileType(taskInfo.getFileUrl()),taskInfo.getFileName(),taskInfo.getFileLength(),taskInfo.getTranTime(),taskInfo.getDirPath() + "/" + taskInfo.getFileName(),taskInfo.getDirPath(),taskInfo.getUpOrDownOrRemote(),sdf.format(calendar.getTime()));
                taskDAO.insertOverTask(fileTran);
            }else if(intent.getAction().equals(MultiRemoteDownloadService.ACTION_UPDATE)){
                int id = intent.getIntExtra("index",0);
                int progress = intent.getIntExtra("downloadProgress",0);
                String speed = intent.getStringExtra("downloadSpeed");
                long finished = intent.getLongExtra("downloadFinished",0);
                long fileLength = intent.getLongExtra("downloadFileLength",0);
                long consumeTime = intent.getLongExtra("downloadUseTime",0);

                List payloadList = new ArrayList();
                payloadList.add("downloadProgress");
                payloadList.add(progress);
                payloadList.add(speed);
                payloadList.add(finished);
                payloadList.add(fileLength);
                payloadList.add(consumeTime);
                myRecTransAdapter.notifyItemChanged(id,payloadList);
            }else if(intent.getAction().equals(MultiRemoteDownloadService.ACTION_FINISHED)){
                //条目下载完成
                TaskInfo taskInfo = (TaskInfo) intent.getSerializableExtra("downloadRemoteFinishFileinfo");

                List payloadList = new ArrayList();
                payloadList.add("downloadOK");
                payloadList.add(100);
                payloadList.add("");
                payloadList.add(taskInfo.getTranFinished());
                payloadList.add(taskInfo.getFileLength());
                myRecTransAdapter.notifyItemChanged(taskInfo.getIndexs(),payloadList);


                //移除完成的条目
//                removeItem(taskInfo);

                //删除任务信息
                taskDAO.deleteTask(taskInfo.getFileUrl());
                //添加下载完成记录
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
                FileTrans fileTran = new FileTrans(0, MyFileUtils.getInstance().checkFileType(taskInfo.getFileUrl()),taskInfo.getFileName(),taskInfo.getFileLength(),taskInfo.getTranTime(),taskInfo.getDirPath() + "/" + taskInfo.getFileName(),taskInfo.getDirPath(),taskInfo.getUpOrDownOrRemote(),sdf.format(calendar.getTime()));
                taskDAO.insertOverTask(fileTran);
            }
        }
    };

    /**
     * 删除某一条目
     * @param taskInfo
     */
    private synchronized void removeItem(TaskInfo taskInfo){
        try {
            taskInfoList.remove(taskInfo.getIndexs());
            myRecTransAdapter.notifyItemRemoved(taskInfo.getIndexs());
        } catch (Exception e) {
            Log.d("zouguo","异常:" + taskInfoList.size());
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        eventFileName = event.getFileName();
        eventFileUrl = event.getEventFileUrl();

        switch (event.getEventType()){
            case 1:
                //SMB下载
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File checkDirExists = new File(file.getPath() + "/com.example.wgjrouter/");

                    if(!checkDirExists.exists()){
                        //不存在该文件夹，新建
                        if(!checkDirExists.mkdirs()){
                            Toast.makeText(getActivity(),"抱歉，创建下载目录失败",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    localDirPath = checkDirExists.getPath();

                    //判断下载队列中是否已存在该任务
                    if(!myRecTransAdapter.isTaskExists(eventFileUrl)){
                        int index = taskInfoList.size();
                        TaskInfo taskInfo = new TaskInfo(0,index,eventFileName,eventFileUrl,localDirPath,"0.0B/s",0,"等待下载",0,0,"/",0,1,false);
                        taskInfoList.add(index,taskInfo);
                        myRecTransAdapter.notifyItemInserted(index);

                        //添加任务到数据库（暂定如此）
                        taskDAO.insertTask(taskInfo);

                        Intent start = new Intent(getActivity(), MultiDownloadService.class);
                        start.setAction(MultiDownloadService.ACTION_START);
                        start.putExtra("fileInfo", taskInfo);
                        getActivity().startService(start);
                    }
                }
                break;
            case 21:
                //重新开始下载任务,偷懒方法
                getUnfinishedDownloadTaskFromDB();
                break;
            case 22:
                //暂停本地下载
                Intent stopLocal = new Intent(getActivity(), MultiDownloadService.class);
                stopLocal.setAction(MultiDownloadService.ACTION_STOP_ALL);
                getActivity().startService(stopLocal);
                //暂停远程下载
                Intent startRemote = new Intent(getActivity(), MultiRemoteDownloadService.class);
                startRemote.setAction(MultiRemoteDownloadService.ACTION_STOP_ALL);
                getActivity().startService(startRemote);
                break;
            case 6:
                //远程下载
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File checkExists = new File(file.getPath() + "/com.example.wgjrouter/");

                    if(!checkExists.exists()){
                        //不存在该文件夹，新建
                        if(!checkExists.mkdirs()){
                            Toast.makeText(getActivity(),"抱歉，创建下载目录失败",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    localDirPath = checkExists.getPath();

                    //判断下载队列中是否已存在该任务
                    if(!myRecTransAdapter.isTaskExists(eventFileUrl)){
                        int index = taskInfoList.size();
                        TaskInfo taskInfo = new TaskInfo(0,index,eventFileName,eventFileUrl,localDirPath,"0.0B/s",0,"等待下载",0,0,"/",0,2,false);
                        taskInfoList.add(index,taskInfo);
                        myRecTransAdapter.notifyItemInserted(index);

                        //添加任务到数据库（暂定如此）
                        taskDAO.insertTask(taskInfo);

                        Intent start = new Intent(getActivity(), MultiRemoteDownloadService.class);
                        start.setAction(MultiRemoteDownloadService.ACTION_START);
                        start.putExtra("fileInfo", taskInfo);
                        getActivity().startService(start);
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("zouguo","onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("zouguo","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("zouguo","onResume");

        //列表全部完成时清空
        List<TaskInfo> tasks = taskDAO.getDownloadTaskInfos();
        if(tasks.size() == 0){
            //数据库中已无下载任务，由于界面在后台时无法刷新，启动时从此处刷新界面
            taskInfoList.clear();//只为了确保为空
            myRecTransAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("zouguo","onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("zouguo","onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("zouguo","onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(mReceive);

        Log.d("zouguo","onDestroy");
    }
}
