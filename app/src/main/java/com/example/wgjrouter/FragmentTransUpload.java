package com.example.wgjrouter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFileOutputStream;

public class FragmentTransUpload extends Fragment {

    private RecyclerView fg_transfer_upload_rv;
    private List<TaskInfo> taskInfoList;
    private MyRecTransAdapter myRecTransAdapter;

    private BufferedInputStream bufferedInputStream = null;
    private SmbFileOutputStream smbFileOutputStream = null;

    private String eventFileName = null;
    private int eventFileType = 0;
    //本地路径
    private String eventDirPath = null;
    private String eventFileUrl = null;

    //路由路径--需要
    private String filePath = null;
    private String dirPath = null;
    private long useTime = 0;

    private boolean isUploadNow = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册接收上传任务广播
        EventBus.getDefault().register(this);
        //注册上传进度更新广播接收器

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fg_trans_upload,null);

        initView(view);

        //从任务数据库中读取未完成的任务(完成的任务会从TaskInfo数据库中删除)
        getUnfinishedUploadTaskFromDB();
        return view;
    }

    private void initView(View view){
        fg_transfer_upload_rv = view.findViewById(R.id.fg_transfer_upload_rv);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        fg_transfer_upload_rv.setLayoutManager(llm);

        taskInfoList = new ArrayList<>();
        myRecTransAdapter = new MyRecTransAdapter(getActivity(), taskInfoList);
        fg_transfer_upload_rv.setAdapter(myRecTransAdapter);
    }

    private void getUnfinishedUploadTaskFromDB(){
        //检查手机备份开关
        SharedPreferences spf = getActivity().getSharedPreferences("app_setting",0);
            //相片、视频备份开关
            boolean upImageState = spf.getBoolean("up_image",false);
            //文件备份开关
            boolean upFilesState = spf.getBoolean("up_file",false);
            //音频备份开关
            boolean upAudioState = spf.getBoolean("up_audio",false);
            //微信文件备份开关
            boolean upWechatState = spf.getBoolean("up_wechat",false);
            //应用备份开关
            boolean upAppState = spf.getBoolean("up_app",false);
            //短信备份开关
            boolean upMsgState = spf.getBoolean("up_msg",false);
            //通话记录备份开关
            boolean upPhoneState = spf.getBoolean("up_phone",false);
            //通讯录备份开关
            boolean upContactState = spf.getBoolean("up_contact",false);
        //获取数据库中未完成的上传任务

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        eventFileName = event.getFileName();
        eventFileType = event.getFileType();
        eventDirPath = event.getEventDirPath();
        eventFileUrl = event.getEventFileUrl();

        dirPath = eventDirPath;
        filePath = eventFileUrl;

        switch (event.getEventType()){
            case 2:
                if(eventFileUrl != null){
                    //上传
                    if(isUploadNow)
                        return;
                    isUploadNow = true;

                    //获取文件File
                    File upfile = new File(eventFileUrl);
                    Log.d("zouguo","UP:" + upfile.getPath());




                }else{
                    Toast.makeText(getActivity(),"抱歉，获取文件路径时发生异常",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    };

    private List<HashMap<String,Object>> putFile(String smbDirUrl, File upfile){
        List<HashMap<String,Object>> list = new ArrayList<>();
        String putLog = "";

        long beginTime = System.currentTimeMillis();

        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(upfile));
            smbFileOutputStream = new SmbFileOutputStream(smbDirUrl + "/" + upfile.getName(),false);

            byte[] bt = new byte[8192];
            int n = 0;
            double total = 0;
            double fiSize = 0;

            fiSize = upfile.length();

            while((n = bufferedInputStream.read(bt)) > 0){
                smbFileOutputStream.write(bt,0,n);
                smbFileOutputStream.flush();

                total += n;
            }

            long endTime = System.currentTimeMillis() - beginTime;

            useTime = endTime;
            putLog = "上传成功:" + upfile.getName() + ",耗时:" + String.valueOf(Math.max(1,endTime / 1000)) + "秒";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            putLog = "上传失败:" + e.getMessage();
        } catch (SmbException e) {
            e.printStackTrace();
            putLog = "上传失败:" + e.getMessage();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            putLog = "上传失败:" + e.getMessage();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            putLog = "上传失败:" + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            putLog = "上传失败:" + e.getMessage();
        } finally {
            try {
                if(null != smbFileOutputStream)
                    smbFileOutputStream.close();
                if(null != bufferedInputStream)
                    bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                putLog = "上传失败:" + e.getMessage();
            }
        }

        HashMap hashMap = new HashMap();
        hashMap.put("putok",true);
        hashMap.put("putlog",putLog);
        list.add(hashMap);
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
