package com.example.wgjrouter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.database.LiveVideoList;
import com.example.utils.MyPhoneUtils;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunnyLiveActivity extends AppCompatActivity{
    private Toolbar funny_tb;
    private ListView funny_live_lv;
    private LinearLayout funny_live_search;
    private EditText funny_live_search_et;
    private Button funny_live_search_btn;
    private ProgressBar funny_live_loading;

    ArrayAdapter<String> aa = null;
    private List<String> nameList = new ArrayList<>();
    private List<String> urlsList = new ArrayList<>();

    //记录播放视频的位置
    private int playItemPosition = 0;
    private int PLAY_CODE = 380;

    private boolean isSearch = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    funny_live_loading.setVisibility(View.GONE);
                    funny_live_lv.setVisibility(View.VISIBLE);
                }
            });

            switch (msg.what) {
                case 0:
                    Toast.makeText(FunnyLiveActivity.this, "抱歉，直播源初始化失败", Toast.LENGTH_SHORT).show();
                    break;
                case 288:
                    Toast.makeText(FunnyLiveActivity.this, "直播源初始化成功", Toast.LENGTH_SHORT).show();
                    showVideoList(nameList);
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
        setContentView(R.layout.activity_funny_live);

        initView();

        /**
         * 初始播放列表
         *
         * 有限数据库读取直播源
         * 数据库中无直播源代表首次运行，此时初始直播源
         */
        if (LitePal.findAll(LiveVideoList.class).size() == 0) {
            Toast.makeText(FunnyLiveActivity.this, "正在初始化直播源列表...", Toast.LENGTH_SHORT).show();

            nameList.clear();
            urlsList.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readVideoListFromTxt();
                }
            }).start();
        } else {
            //从数据库读取数据
            nameList.clear();
            urlsList.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readVideoListFromDB();
                }
            }).start();
        }
    }

    private void initView() {
        funny_live_loading = findViewById(R.id.funny_live_loading);
        funny_tb = findViewById(R.id.funny_tb);
        funny_live_lv = findViewById(R.id.funny_live_lv);
        funny_live_search = findViewById(R.id.funny_live_search);
        funny_live_search_et = findViewById(R.id.funny_live_search_et);
        funny_live_search_btn = findViewById(R.id.funny_live_search_btn);

        aa = new ArrayAdapter<>(FunnyLiveActivity.this, R.layout.item_lv_listvideo_black, nameList);
        funny_live_lv.setAdapter(aa);
        aa.notifyDataSetChanged();

        funny_live_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funny_live_search.setVisibility(View.GONE);
                MyPhoneUtils.getInstance(FunnyLiveActivity.this).hideInputMethod(funny_live_search_et);

                if(!funny_live_search_et.getText().toString().equals("")){
                    funny_tb.setNavigationIcon(R.mipmap.rollback);
                    funny_tb.setTitle("搜索结果");
                    isSearch = true;

                    nameList.clear();
                    urlsList.clear();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            searchLiveVideo("%" + funny_live_search_et.getText().toString() + "%");
                        }
                    }).start();
                }
            }
        });

        funny_live_search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == KeyEvent.KEYCODE_SEARCH){
                    funny_live_search.setVisibility(View.GONE);
                    MyPhoneUtils.getInstance(FunnyLiveActivity.this).hideInputMethod(funny_live_search_et);

                    if(!funny_live_search_et.getText().toString().equals("")){
                        funny_tb.setNavigationIcon(R.mipmap.rollback);
                        funny_tb.setTitle("搜索结果");
                        isSearch = true;

                        nameList.clear();
                        urlsList.clear();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                searchLiveVideo("%" + funny_live_search_et.getText().toString() + "%");
                            }
                        }).start();
                    }
                }
                return true;
            }
        });

        funny_tb.setNavigationIcon(R.mipmap.back);
        funny_tb.setTitle("直播源列表");
        funny_tb.inflateMenu(R.menu.videolive_play_option_menu);

        funny_tb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.live_menu_query:
                        if(funny_live_search.getVisibility() == View.GONE){
                            funny_live_search.setVisibility(View.VISIBLE);
                        }else{
                            funny_live_search.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.live_menu_collect:
                        Intent gocollect = new Intent(FunnyLiveActivity.this,FunnyLiveCollectActivity.class);
                        startActivity(gocollect);
                        break;
                    case R.id.live_menu_reback:
                        AlertDialog ad = null;
                        AlertDialog.Builder adb = new AlertDialog.Builder(FunnyLiveActivity.this);

                        adb.setTitle("是否初始直播源列表")
                                .setMessage("删除、新增的直播源都将被丢弃，仅恢复内置的直播源")
                                .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        funny_live_loading.setVisibility(View.VISIBLE);
                                        funny_live_lv.setVisibility(View.INVISIBLE);

                                        //清空数据库中的直播源数据
                                        LitePal.deleteAll(LiveVideoList.class);

                                        Toast.makeText(FunnyLiveActivity.this, "正在初始化直播源列表...", Toast.LENGTH_SHORT).show();

                                        nameList.clear();
                                        urlsList.clear();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                readVideoListFromTxt();
                                            }
                                        }).start();
                                    }
                                });
                        ad = adb.create();
                        ad.show();

                        ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                        ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                        break;
                    case R.id.live_menu_recorder:
                        Intent gorecorder = new Intent(FunnyLiveActivity.this, FunnyLiveRecorderActivity.class);
                        startActivity(gorecorder);
                        break;
                }
                return false;
            }
        });

        funny_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSearch){
                    funny_tb.setNavigationIcon(R.mipmap.back);
                    funny_tb.setTitle("直播源列表");
                    isSearch = false;

                    //回到直播源列表
                    nameList.clear();
                    urlsList.clear();
                    readVideoListFromDB();
                }else{
                    finish();
                }
            }
        });

        funny_live_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playItemPosition = position;

                //加入播放记录
                LiveVideoList lvl = LitePal.where("liveName = ? and liveUrls = ?", nameList.get(position), urlsList.get(position)).findFirst(LiveVideoList.class);
                lvl.setIsPlay(1);
                lvl.save();

                //解析出直播源名称、链接
                Intent golive = new Intent(FunnyLiveActivity.this, VideoPlayVTM.class);
                golive.putExtra("videoType", 1);
                golive.putExtra("videoUrl", urlsList.get(position));
                golive.putExtra("videoTitle", nameList.get(position));
                golive.putExtra("videoPosition", playItemPosition);
                startActivityForResult(golive,PLAY_CODE);
            }
        });

        funny_live_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int parentPosition, long id) {
                View viewPoP = View.inflate(FunnyLiveActivity.this,R.layout.dialog_file_longclick_menu,null);

                ListView listView = viewPoP.findViewById(R.id.dialog_file_longclick_menu_lv);

                String[] menuData = {"删除","收藏","取消"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FunnyLiveActivity.this,android.R.layout.simple_list_item_1,menuData);
                listView.setAdapter(arrayAdapter);

                final PopupWindow popupWindow = new PopupWindow(viewPoP, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popupWindow.dismiss();

                        switch (position){
                            case 0:
                                LiveVideoList lvld = LitePal.where("liveName = ? and liveUrls = ?",nameList.get(parentPosition),urlsList.get(parentPosition)).findFirst(LiveVideoList.class);
                                lvld.delete();

                                nameList.clear();
                                urlsList.clear();
                                readVideoListFromDB();
                                break;
                            case 1:
                                LiveVideoList lvlc = LitePal.where("liveName = ? and liveUrls = ?",nameList.get(parentPosition),urlsList.get(parentPosition)).findFirst(LiveVideoList.class);
                                lvlc.setIsCollect(1);
                                lvlc.save();

                                Toast.makeText(FunnyLiveActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
                                nameList.clear();
                                urlsList.clear();
                                readVideoListFromDB();
                                break;
                        }
                    }
                });

                ColorDrawable cd = new ColorDrawable(0xb0000000);
                popupWindow.setBackgroundDrawable(cd);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(funny_live_lv, Gravity.CENTER,0,0);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLAY_CODE && resultCode == RESULT_OK){
            playItemPosition = data.getIntExtra("videoPosition",0);

            //设置ListView到指定位置
            funny_live_lv.setSelection(playItemPosition);
        }
    }

    private void readVideoListFromTxt() {
        try {
            InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("livevideolist.txt"));
            BufferedReader br = new BufferedReader(isr);

            String line = "";

            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");

                if (items.length == 2) {
                    nameList.add(items[0]);
                    urlsList.add(items[1]);

                    //存入数据库
                    LiveVideoList lvl = new LiveVideoList();
                    lvl.setLiveName(items[0]);
                    lvl.setLiveUrls(items[1]);
                    lvl.save();
                }
            }

            Message msg = new Message();
            msg.what = 288;
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = 0;
            handler.sendMessage(msg);
        }
    }

    private void readVideoListFromDB() {
        List<LiveVideoList> allVideoList = LitePal.findAll(LiveVideoList.class);

        for (LiveVideoList lvl : allVideoList) {
            nameList.add(lvl.getLiveName());
            urlsList.add(lvl.getLiveUrls());
        }

        Message msg = new Message();
        msg.what = 289;
        handler.sendMessage(msg);
    }

    private void showVideoList(List<String> nameList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aa.notifyDataSetChanged();
            }
        });
    }

    private void searchLiveVideo(String liveName){
        List<LiveVideoList> list = LitePal.where("livename like ?",liveName).find(LiveVideoList.class);

        for(LiveVideoList lvl : list){
            nameList.add(lvl.getLiveName());
            urlsList.add(lvl.getLiveUrls());
        }

        Message msg = new Message();
        msg.what = 289;
        handler.sendMessage(msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}