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

public class FunnyLiveCollectActivity extends AppCompatActivity{
    private Toolbar funny_collect_tb;
    private ListView funny_live_collect_lv;

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
                    Toast.makeText(FunnyLiveCollectActivity.this,"抱歉，收藏夹读取失败",Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_funny_collect);

        initView();

        readVideoCollectListFromDB();
    }

    private void initView(){
        funny_collect_tb = findViewById(R.id.funny_collect_tb);
        funny_live_collect_lv = findViewById(R.id.funny_live_collect_lv);

        aa = new ArrayAdapter<>(FunnyLiveCollectActivity.this,R.layout.item_lv_listvideo_black,nameList);
        funny_live_collect_lv.setAdapter(aa);

        funny_collect_tb.setNavigationIcon(R.mipmap.back);
        funny_collect_tb.setTitle("收藏夹");
        funny_collect_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        funny_live_collect_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playItemPosition = position;
                //解析出直播源名称、链接
                Intent golive = new Intent(FunnyLiveCollectActivity.this, VideoPlayVTM.class);
                golive.putExtra("videoType",3);
                golive.putExtra("videoUrl",urlsList.get(position));
                golive.putExtra("videoTitle",nameList.get(position));
                golive.putExtra("videoPosition",playItemPosition);
                startActivity(golive);
            }
        });
    }

    private void readVideoCollectListFromDB(){
        nameList.clear();
        urlsList.clear();

        List<LiveVideoList> allVideoList = LitePal.where("iscollect = ?","1").find(LiveVideoList.class);

        for (LiveVideoList lvl : allVideoList){
            nameList.add(lvl.getLiveName());
            urlsList.add(lvl.getLiveUrls());
        }

        Message msg = new Message();
        msg.what = 289;
        handler.sendMessage(msg);
    }

    private void showVideoList(List<String> nameList){
        aa.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readVideoCollectListFromDB();
    }
}
