package com.example.wgjrouter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.adapter.MyRecBackupsAdapter;
import com.example.bean.MyPhoneBackups;

import java.util.ArrayList;
import java.util.List;

public class BackupListViewActivity extends AppCompatActivity {

    private Toolbar activity_phonebackup_list_tb;
    private RecyclerView activity_phonebackup_list_rv;

    String[] itemTitles = {"相册备份","文件备份","音频备份","微信文件备份","应用备份","短信备份","通话记录备份","通讯录备份"};

    List<MyPhoneBackups> myPhoneBackupsList;
    MyRecBackupsAdapter myRecBackupsAdapter;

    List<Boolean> autoUploadState = new ArrayList<>();

    private int REQUEST_CODE = 1689;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_backup_list);

        initView();

        checkAutoUploadState();

        showDataView();
    }

    private void initView(){
        activity_phonebackup_list_tb = findViewById(R.id.activity_phonebackup_list_tb);
        activity_phonebackup_list_rv = findViewById(R.id.activity_phonebackup_list_rv);

        activity_phonebackup_list_tb.setTitle("手机备份");
        activity_phonebackup_list_tb.setNavigationIcon(R.mipmap.back);
        activity_phonebackup_list_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(BackupListViewActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        activity_phonebackup_list_rv.setLayoutManager(llm);

        myPhoneBackupsList = new ArrayList<>();
        myRecBackupsAdapter = new MyRecBackupsAdapter(BackupListViewActivity.this,myPhoneBackupsList);
        activity_phonebackup_list_rv.setAdapter(myRecBackupsAdapter);

        myRecBackupsAdapter.setOnItemListener(new MyRecBackupsAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent gointent;
                switch (position){
                    case 0:
                        gointent = new Intent(BackupListViewActivity.this,BackupImageActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 1:
                        gointent = new Intent(BackupListViewActivity.this,BackupFilesActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 2:
                        gointent = new Intent(BackupListViewActivity.this,BackupAudioActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 3:
                        gointent = new Intent(BackupListViewActivity.this,BackupWechatActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 4:
                        gointent = new Intent(BackupListViewActivity.this,BackupAppActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 5:
                        gointent = new Intent(BackupListViewActivity.this,BackupMsgActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 6:
                        gointent = new Intent(BackupListViewActivity.this,BackupPhoneActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                    case 7:
                        gointent = new Intent(BackupListViewActivity.this, BackupContactActivity.class);
                        startActivityForResult(gointent,REQUEST_CODE);
                        break;
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE){
            int open_type = data.getIntExtra("open_type",0);
            boolean open_state = data.getBooleanExtra("open_state",false);

            myRecBackupsAdapter.notifyItemChanged(open_type,open_state);
        }
    }

    private void checkAutoUploadState(){
        autoUploadState.clear();

        SharedPreferences spf = getSharedPreferences("app_setting",MODE_PRIVATE);
        boolean upImageState = spf.getBoolean("up_image",false);
        boolean upFilesState = spf.getBoolean("up_file",false);
        boolean upAudioState = spf.getBoolean("up_audio",false);
        boolean upWechatState = spf.getBoolean("up_wechat",false);
        boolean upAppState = spf.getBoolean("up_app",false);
        boolean upMsgState = spf.getBoolean("up_msg",false);
        boolean upPhoneState = spf.getBoolean("up_phone",false);
        boolean upContactState = spf.getBoolean("up_contact",false);

        autoUploadState.add(upImageState);
        autoUploadState.add(upFilesState);
        autoUploadState.add(upAudioState);
        autoUploadState.add(upWechatState);
        autoUploadState.add(upAppState);
        autoUploadState.add(upMsgState);
        autoUploadState.add(upPhoneState);
        autoUploadState.add(upContactState);
    }

    private void showDataView(){
        myPhoneBackupsList.clear();

        for(int i = 0;i < itemTitles.length;i++){
            MyPhoneBackups myPhoneBackup = new MyPhoneBackups();
            myPhoneBackup.setFileType(i);
            myPhoneBackup.setFileTitle(itemTitles[i]);
            myPhoneBackup.setAutoUpload(autoUploadState.get(i));
            myPhoneBackupsList.add(myPhoneBackup);
        }
        myRecBackupsAdapter.notifyDataSetChanged();
    }
}
