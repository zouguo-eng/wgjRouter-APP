package com.example.wgjrouter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.adapter.MyRecImageBackupAdapter;
import com.example.bean.MyPhoneBackups;
import com.example.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BackupImageActivity extends AppCompatActivity {
    private Toolbar activity_backup_image_tb;
    private SwitchCompat activity_backup_image_sc;
    private SwitchCompat activity_backup_video_sc;
    private TextView activity_backup_image_wz;
    private TextView activity_backup_image_count_tips;
    private TextView activity_backup_image_selectall;
    private RecyclerView activity_backup_image_rv;
    private RelativeLayout activity_backup_video_rl;

    List<String> dirTitleList = new ArrayList<>();
    List<Integer> dirItemCount = new ArrayList<>();
    List<String> dirFirstItem = new ArrayList<>();
    Set<String> dirUpList = new ArraySet<>();

    List<MyPhoneBackups> myPhoneBackupsList;
    MyRecImageBackupAdapter myRecImageBackupAdapter;

    private EventBus eventBus;

    SharedPreferences sp;
    SharedPreferences.Editor spe;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 12121:
                    //数据获取成功，展示
                    showDataView();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_backup_image);

        eventBus = EventBus.getDefault();
        eventBus.register(this);

        initView();

        initImageAutoBackupState();
    }

    private void initView(){
        sp = getSharedPreferences("app_setting",MODE_PRIVATE);
        spe = sp.edit();

        Set<String> autoUpDirList = sp.getStringSet("up_image_dirs",new HashSet<String>());

        dirUpList = sp.getStringSet("up_image_dirs",null);

        activity_backup_image_tb = findViewById(R.id.activity_backup_image_tb);
        activity_backup_image_sc = findViewById(R.id.activity_backup_image_sc);
        activity_backup_video_sc = findViewById(R.id.activity_backup_video_sc);
        activity_backup_image_count_tips = findViewById(R.id.activity_backup_image_count_tips);
        activity_backup_image_selectall = findViewById(R.id.activity_backup_image_selectall);
        activity_backup_image_wz = findViewById(R.id.activity_backup_image_wz);
        activity_backup_image_rv = findViewById(R.id.activity_backup_image_rv);
        activity_backup_video_rl = findViewById(R.id.activity_backup_video_rl);

        activity_backup_image_selectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myRecImageBackupAdapter.isSelectAll()){
                    //取消全选
                    myRecImageBackupAdapter.selectNone();
                }else{
                    //全选
                    myRecImageBackupAdapter.selectAll();
                }
            }
        });

        activity_backup_image_tb.setTitle("相册备份");
        activity_backup_image_tb.setNavigationIcon(R.mipmap.back);
        activity_backup_image_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIntentData();
                finish();
            }
        });

        activity_backup_image_wz.setText("照片及视频备份至：来自：" + Build.MODEL);

        activity_backup_image_sc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spe.putBoolean("up_image",true);
                    spe.commit();
                    activity_backup_image_rv.setVisibility(View.VISIBLE);
                    activity_backup_video_rl.setVisibility(View.VISIBLE);
                    getAllImageFromLocal();
                }else{
                    spe.putBoolean("up_image",false);
                    spe.putBoolean("up_video",false);
                    spe.commit();
                    activity_backup_video_sc.setChecked(false);
                    activity_backup_image_rv.setVisibility(View.GONE);
                    activity_backup_video_rl.setVisibility(View.GONE);
                    //移除全部列表
                    myPhoneBackupsList.clear();
                    myRecImageBackupAdapter.setCanInitData(true);
                    myRecImageBackupAdapter.notifyDataSetChanged();
                }
            }
        });

        activity_backup_video_sc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spe.putBoolean("up_video",true);
                    spe.commit();
                    getAllVideoFromLocal();
                }else{
                    spe.putBoolean("up_video",false);
                    spe.commit();
                    //移除全部列表
                    myPhoneBackupsList.clear();
                    myRecImageBackupAdapter.setCanInitData(true);
                    myRecImageBackupAdapter.notifyDataSetChanged();
                    //重新加载图片
                    getAllImageFromLocal();
                }
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(BackupImageActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        activity_backup_image_rv.setLayoutManager(llm);

        myPhoneBackupsList = new ArrayList<>();
        myRecImageBackupAdapter = new MyRecImageBackupAdapter(BackupImageActivity.this, eventBus, myPhoneBackupsList, autoUpDirList);
        activity_backup_image_rv.setAdapter(myRecImageBackupAdapter);

        myRecImageBackupAdapter.setOnItemListener(new MyRecImageBackupAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                SwitchCompat sc = view.findViewById(R.id.item_rec_autobackup_sc);

                if(sc.isChecked()){
                    myRecImageBackupAdapter.notifyItemChanged(position,false);
                }else{
                    myRecImageBackupAdapter.notifyItemChanged(position,true);
                }
            }
        });

        activity_backup_image_rv.addItemDecoration(new DividerItemDecoration(BackupImageActivity.this,DividerItemDecoration.VERTICAL));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getEventType()) {
            case 33:
                if(event.getFileType() == dirTitleList.size()){
                    //当前全选
                    activity_backup_image_selectall.setText("全不选");
                }else{
                    //全不选
                    activity_backup_image_selectall.setText("全选");
                }

                activity_backup_image_count_tips.setText("选择自动备份的相册  " + event.getFileType() + "/" + dirTitleList.size());
                break;
        }
    }

    private void initImageAutoBackupState(){
        boolean imageAutoState = sp.getBoolean("up_image",false);
        boolean videoAutoState = sp.getBoolean("up_video",false);

        if(imageAutoState){
            activity_backup_image_sc.setChecked(true);
            activity_backup_video_rl.setVisibility(View.VISIBLE);
        }else{
            activity_backup_image_sc.setChecked(false);
            activity_backup_video_rl.setVisibility(View.GONE);
        }

        if(videoAutoState){
            activity_backup_video_sc.setChecked(true);
        }else{
            activity_backup_video_sc.setChecked(false);
        }
    }

    private void getAllImageFromLocal(){
        dirTitleList.clear();
        dirItemCount.clear();
        dirFirstItem.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
                Log.d("zouguo","CountImage:" + cursor.getCount());

                while (cursor.moveToNext()){
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String dir = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                    if(location.contains("tencent")){
                        dir = "tencent";
                    }

                    //判断文件夹是否已记录
                    if(dirTitleList.contains(dir)){
                        int index = dirTitleList.indexOf(dir);
                        int count = dirItemCount.get(index) + 1;

                        dirItemCount.set(index,count);
                    }else{
                        dirTitleList.add(dir);
                        dirItemCount.add(1);
                        dirFirstItem.add(location);
                    }
                }

                Message msg = new Message();
                msg.what = 12121;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void getAllVideoFromLocal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
                Log.d("zouguo","CountVideo:" + cursor.getCount());

                while (cursor.moveToNext()){
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String dir = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                    if(location.contains("tencent")){
                        dir = "tencent";
                    }

                    //判断文件夹是否已记录
                    if(dirTitleList.contains(dir)){
                        int index = dirTitleList.indexOf(dir);
                        int count = dirItemCount.get(index) + 1;

                        dirItemCount.set(index,count);
                    }else{
                        dirTitleList.add(dir);
                        dirItemCount.add(1);
                        dirFirstItem.add(location);
                    }
                }

                Message msg = new Message();
                msg.what = 12121;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void showDataView(){
        myPhoneBackupsList.clear();

        int autoCounts = 0;

        if(activity_backup_image_sc.isChecked()){
            for(int i = 0;i<dirTitleList.size();i++){
                MyPhoneBackups myPhoneBackup = new MyPhoneBackups();
                myPhoneBackup.setFileTitle(dirTitleList.get(i));
                myPhoneBackup.setFileCounts(dirItemCount.get(i));

                if(dirUpList != null && dirUpList.contains(dirTitleList.get(i))){
                    myPhoneBackup.setAutoUpload(true);
                    autoCounts++;
                }else{
                    myPhoneBackup.setAutoUpload(false);
                }

                myPhoneBackup.setFirstItemUrl(dirFirstItem.get(i));
                myPhoneBackupsList.add(myPhoneBackup);
            }
            myRecImageBackupAdapter.setCanInitData(true);
            myRecImageBackupAdapter.notifyDataSetChanged();


            activity_backup_image_count_tips.setText("选择自动备份的相册  "  + autoCounts + "/" + dirTitleList.size());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            backIntentData();
            finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Set<String> list = new ArraySet<>();

        if(activity_backup_image_sc.isChecked()){
            //记录需要进行自动上传操作的图片、视频文件夹
            List<Integer> selectListIndex = myRecImageBackupAdapter.getSelectItemIndex();

            for(int i = 0;i < selectListIndex.size();i++){
                list.add(dirTitleList.get(selectListIndex.get(i)));
            }

            spe.putStringSet("up_image_dirs",list);
        }else{
            //清空设置的上传文件夹
            spe.putStringSet("up_image_dirs",list);
        }
        spe.commit();
    }

    //返回时携带开启状态数据
    private void backIntentData(){
        Intent backdata = new Intent();
        backdata.putExtra("open_type",0);
        backdata.putExtra("open_state",activity_backup_image_sc.isChecked());
        setResult(RESULT_OK,backdata);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
    }
}
