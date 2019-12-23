package com.example.wgjrouter;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adapter.MyBaGvPhoneBackupAdapter;
import com.example.bean.MyPhoneBackups;

import java.util.ArrayList;
import java.util.List;

public class BackupGridViewActivity extends AppCompatActivity {

    List<MyPhoneBackups> myPhoneBackupsList;
    MyBaGvPhoneBackupAdapter myBaGvPhoneBackupAdapter;

    List<String> imageTitleList = new ArrayList<>();
    List<Integer> imageSizeList = new ArrayList<>();
    List<String> imageUrlList = new ArrayList<>();
    List<String> videoTitleList = new ArrayList<>();
    List<Integer> videoSizeList = new ArrayList<>();
    List<String> videoUrlList = new ArrayList<>();
    List<String> audioTitleList = new ArrayList<>();
    List<Integer> audioSizeList = new ArrayList<>();
    List<String> audioUrlList = new ArrayList<>();
    List<String> docsTitleList = new ArrayList<>();
    List<Integer> docsSizeList = new ArrayList<>();
    List<String> docsUrlList = new ArrayList<>();

    private ProgressBar activity_phonebackup_pb;
    private Toolbar activity_phonebackup_tb;
    private GridView activity_phonebackup_gv;
    private RecyclerView activity_phonebackup_rc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_backup_grid);

        initView();

        activity_phonebackup_pb.setVisibility(View.VISIBLE);
        getAllImageFromLocal();
        getAllVideoFromLocal();
        getAllAudioFromLocal();
//        getAllFileFromLocal();
        activity_phonebackup_pb.setVisibility(View.GONE);

        showDataView();
    }

    private void initView(){
        activity_phonebackup_pb = findViewById(R.id.activity_phonebackup_pb);
        activity_phonebackup_tb = findViewById(R.id.activity_phonebackup_tb);
        activity_phonebackup_gv = findViewById(R.id.activity_phonebackup_gv);
        activity_phonebackup_rc = findViewById(R.id.activity_phonebackup_rc);

        activity_phonebackup_tb.setTitle("本地文件列表");
        activity_phonebackup_tb.setNavigationIcon(R.mipmap.back);
        activity_phonebackup_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myPhoneBackupsList = new ArrayList<>();
        myBaGvPhoneBackupAdapter = new MyBaGvPhoneBackupAdapter(BackupGridViewActivity.this, myPhoneBackupsList);
        activity_phonebackup_gv.setAdapter(myBaGvPhoneBackupAdapter);
    }

    private void getAllImageFromLocal(){
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        Log.d("zouguo","CountImage:" + cursor.getCount());

        Toast.makeText(BackupGridViewActivity.this,"数量：" + cursor.getCount(),Toast.LENGTH_SHORT).show();

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            Log.d("zouguo","name1:" + name);
            Log.d("zouguo","size1:" + size);
            Log.d("zouguo","位置1：" + location);

            imageTitleList.add(name);
            imageSizeList.add(size);
            imageUrlList.add(location);
        }
    }

    private void getAllVideoFromLocal(){
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        Log.d("zouguo","CountVideo:" + cursor.getCount());

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

            Log.d("zouguo","name2:" + name);
            Log.d("zouguo","size2:" + size);
            Log.d("zouguo","位置2：" + location);

            videoTitleList.add(name);
            videoSizeList.add(size);
            videoUrlList.add(location);
        }
    }

    private void getAllAudioFromLocal(){
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        Log.d("zouguo","CountAudio:" + cursor.getCount());

        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

            Log.d("zouguo","name3:" + name);
            Log.d("zouguo","size3:" + size);
            Log.d("zouguo","位置3：" + location);

            audioTitleList.add(name);
            audioSizeList.add(size);
            audioUrlList.add(location);
        }
    }

    private void getAllFileFromLocal(){
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"),null,null,null,null);
        Log.d("zouguo","CountFile:" + cursor.getCount());
        Log.d("zouguo","ColumnCount:" + cursor.getColumnCount());

        for(int i = 0;i < cursor.getColumnCount(); i++){
            Log.d("zouguo","Column" + i + ":" + cursor.getColumnName(i));
        }

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String mimetype = cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow("_size"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("_data"));


            Log.d("zouguo","------------------------------------");

            Log.d("zouguo","name4:" + name);
            Log.d("zouguo","title4:" + title);
            Log.d("zouguo","mimetype4:" + mimetype);
            Log.d("zouguo","size4:" + size);
            Log.d("zouguo","位置4：" + location);
        }
    }

    private void showDataView(){
        int imageCounts = imageTitleList.size();
        int videoCounts = videoTitleList.size();
        int audioCounts = audioTitleList.size();

        MyPhoneBackups images = new MyPhoneBackups();
        images.setFileType(0);
        images.setFileTitle("图片");
        images.setFileCounts(imageCounts);
        myPhoneBackupsList.add(images);

        MyPhoneBackups videos = new MyPhoneBackups();
        videos.setFileType(1);
        videos.setFileTitle("视频");
        videos.setFileCounts(videoCounts);
        myPhoneBackupsList.add(videos);

        MyPhoneBackups audios = new MyPhoneBackups();
        audios.setFileType(2);
        audios.setFileTitle("音频");
        audios.setFileCounts(audioCounts);
        myPhoneBackupsList.add(audios);

        myBaGvPhoneBackupAdapter.notifyDataSetChanged();
    }
}
