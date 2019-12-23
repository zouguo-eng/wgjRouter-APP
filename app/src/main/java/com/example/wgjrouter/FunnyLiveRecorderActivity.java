package com.example.wgjrouter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.database.LiveVideoList;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FunnyLiveRecorderActivity extends AppCompatActivity{
    private Toolbar funny_recorder_tb;
    private ListView funny_live_recorder_lv;

    ArrayAdapter<String> aa = null;
    private List<String> nameList = new ArrayList<>();
    private List<String> urlsList = new ArrayList<>();

    //记录播放视频的位置
    private int playItemPosition = 0;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(FunnyLiveRecorderActivity.this,"抱歉，播放记录读取失败",Toast.LENGTH_SHORT).show();
                    break;
                case 289:
                    showVideoList(nameList);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funny_recorder);

        initView();

        nameList.clear();
        urlsList.clear();

        readVideoRecorderListFromDB();
    }

    private void initView(){
        funny_recorder_tb = findViewById(R.id.funny_recorder_tb);
        funny_live_recorder_lv = findViewById(R.id.funny_live_recorder_lv);

        funny_recorder_tb.setNavigationIcon(R.mipmap.back);
        funny_recorder_tb.setTitle("播放记录");
        funny_recorder_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        funny_live_recorder_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playItemPosition = position;
                //解析出直播源名称、链接
                Intent golive = new Intent(FunnyLiveRecorderActivity.this, VideoPlayVTM.class);
                golive.putExtra("videoType",2);
                golive.putExtra("videoUrl",urlsList.get(position));
                golive.putExtra("videoTitle",nameList.get(position));
                golive.putExtra("videoPosition",playItemPosition);
                startActivity(golive);
            }
        });
    }

    private void readVideoRecorderListFromDB(){
        List<LiveVideoList> allVideoList = LitePal.where("isplay = ?","1").find(LiveVideoList.class);

        for (LiveVideoList lvl : allVideoList){
            nameList.add(lvl.getLiveName());
            urlsList.add(lvl.getLiveUrls());
        }

        Message msg = new Message();
        msg.what = 289;
        handler.sendMessage(msg);
    }

    private void showVideoList(List<String> nameList){
        aa = new ArrayAdapter<>(FunnyLiveRecorderActivity.this,R.layout.item_lv_listvideo_black,nameList);
        funny_live_recorder_lv.setAdapter(aa);
    }
}
