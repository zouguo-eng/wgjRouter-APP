package com.example.wgjrouter;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adapter.MyRecRemoteFileAdapter;
import com.example.bean.MyRecRemoteFileInfos;
import com.example.event.MessageEvent;
import com.example.samba.SambaUtils;
import com.example.utils.MyFileUtils;
import com.example.utils.MyPhoneUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteFileActivity extends AppCompatActivity {
    private boolean canBack = false;
    private int clickPicIndex = 0;

    private String fatherUrl = null;
    private String webHost = null;
    private String webRootUrl = null;
    private List<Boolean> fileOrDirList = new ArrayList<>();
    private List<String> titList = new ArrayList<>();
    private List<String> urlList = new ArrayList<>();
    private List<String> picItemUrlList = new ArrayList<>();
    private ArrayList<String> videoTitleList = new ArrayList<>();
    private ArrayList<String> videoItemUrlList = new ArrayList<>();
    private List<Integer> fileTypeList = new ArrayList<>();//标记文件的类型，下载用
    List<MyRecRemoteFileInfos> myRecRemoteFileInfosList = null;
    MyRecRemoteFileAdapter myRecRemoteFileAdapter;

    private ProgressBar remote_file_pb;
    private FrameLayout remote_file_parent;//弹窗的父布局
    private PopupWindow popupWindowPic;//查看图片的弹窗
    private Toolbar remote_file_tb;
    private RecyclerView remote_file_rv;
    private View remote_file_opendir_cl;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remote_file_pb.setVisibility(View.GONE);
                }
            });

            switch (msg.what){
                case 1567:
                    Toast.makeText(RemoteFileActivity.this,"失败" + msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 1568:
                    analyResponse(webHost,msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_file);

        Intent intent = getIntent();
        webHost = intent.getStringExtra("web_host");
        webRootUrl = intent.getStringExtra("web_url");

        initView();
        getRemoteSource(webRootUrl);

        EventBus.getDefault().register(RemoteFileActivity.this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getEventType()) {
            case 4:
                //下载成功
                break;
        }
    }

    private void initView(){
        remote_file_pb = findViewById(R.id.remote_file_pb);
        remote_file_parent = findViewById(R.id.remote_file_parent);
        remote_file_tb = findViewById(R.id.remote_file_tb);
        remote_file_rv = findViewById(R.id.remote_file_rv);
        remote_file_opendir_cl = findViewById(R.id.remote_file_opendir_cl);

        remote_file_tb.setTitle("远程文件列表");
        remote_file_tb.setNavigationIcon(R.mipmap.back);
        remote_file_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager llManager = new LinearLayoutManager(RemoteFileActivity.this);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        remote_file_rv.setLayoutManager(llManager);

        myRecRemoteFileInfosList = new ArrayList<>();
        myRecRemoteFileAdapter = new MyRecRemoteFileAdapter(RemoteFileActivity.this,myRecRemoteFileInfosList);
        remote_file_rv.setAdapter(myRecRemoteFileAdapter);

        myRecRemoteFileAdapter.setOnItemListener(new MyRecRemoteFileAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(fileOrDirList.get(position)){
                    //文件
                    String fileUrl = urlList.get(position);

                    int label = MyFileUtils.getInstance().checkFileType(fileUrl);

                    showFile(label,fileUrl);
                }else{
                    //文件夹，进入子目录
                    canBack = true;
                    getRemoteSource(urlList.get(position));
                }
            }

            @Override
            public void onItemLongClick(View view, final int parentPosition) {
                View viewPoP = View.inflate(RemoteFileActivity.this,R.layout.dialog_file_longclick_menu,null);

                ListView listView = viewPoP.findViewById(R.id.dialog_file_longclick_menu_lv);

                String[] menuData = {"下载","取消"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RemoteFileActivity.this,android.R.layout.simple_list_item_1,menuData);
                listView.setAdapter(arrayAdapter);

                final PopupWindow popupWindow = new PopupWindow(viewPoP,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                        popupWindow.dismiss();

                        switch (position){
                            case 0:
                                //下载
                                if(fileOrDirList.get(parentPosition)){
                                    //判断读写权限是否正常
                                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                                        String[]  needPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                        List<String> losePermissions = new ArrayList<>();
                                        for(int i = 0;i < needPermissions.length;i++){
                                            if(checkSelfPermission(needPermissions[i]) != PackageManager.PERMISSION_GRANTED){
                                                losePermissions.add(needPermissions[i]);
                                            }
                                        }
                                        if(!losePermissions.isEmpty()){
                                            //权限不正常
                                            Toast.makeText(RemoteFileActivity.this,"未能获取到存储器读写权限",Toast.LENGTH_SHORT).show();
                                            return;
                                        }else{
                                            //权限都正常
                                            //检测本地Download目录是否已存在该文件
                                            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                                File checkExists = new File(file.getPath() + "/com.example.wgjrouter/" + titList.get(parentPosition));

                                                if(!checkExists.exists()){
                                                    EventBus.getDefault().postSticky(new MessageEvent(6,fileTypeList.get(parentPosition),titList.get(parentPosition),2,"",urlList.get(parentPosition)));
                                                    Toast.makeText(RemoteFileActivity.this,"已添加到下载队列",Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(RemoteFileActivity.this,"本地已存在该文件",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }else{
                                        //低版本，不需要敏感权限确认
                                        //检测本地Download目录是否已存在该文件
                                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            File checkExists = new File(file.getPath() + "/com.example.wgjrouter/" + titList.get(parentPosition));

                                            if(!checkExists.exists()){
                                                EventBus.getDefault().postSticky(new MessageEvent(6,fileTypeList.get(parentPosition),titList.get(parentPosition),2,"",urlList.get(parentPosition)));
                                                Toast.makeText(RemoteFileActivity.this,"已添加到下载队列",Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(RemoteFileActivity.this,"本地已存在该文件",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }else{
                                    Toast.makeText(RemoteFileActivity.this,"抱歉，暂不支持下载文件夹",Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                });

                ColorDrawable cd = new ColorDrawable(0xb0000000);
                popupWindow.setBackgroundDrawable(cd);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);

                popupWindow.showAtLocation(remote_file_parent,Gravity.CENTER,0,0);
            }
        });
    }

    private void getRemoteSource(String webUrl){
        if(webUrl.equals(webRootUrl + "../")){
            canBack = false;
           return;
        }

        remote_file_pb.setVisibility(View.VISIBLE);

        OkHttpClient ohClient = new OkHttpClient();
        Request req = new Request.Builder()
                .url(webUrl)
                .method("GET",null)
                .build();
        Call call = ohClient.newCall(req);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Message msg = new Message();
                msg.what = 1567;
                msg.obj = e.getMessage();
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Message msg = new Message();
                msg.what = 1568;
                msg.obj = response.body().string();
                handler.sendMessage(msg);
            }
        });
        handler.removeCallbacksAndMessages(null);
    }

    private void analyResponse(String webHost,String resBody){
        if(webHost == null){
            return;
        }

        myRecRemoteFileInfosList.clear();
        fileOrDirList.clear();
        titList.clear();
        urlList.clear();
        picItemUrlList.clear();
        videoItemUrlList.clear();
        videoTitleList.clear();
        fileTypeList.clear();

        Document doc = Jsoup.parse(resBody);
        Elements elements = doc.getElementsByTag("strong");

        //设置标题
        remote_file_tb.setTitle(doc.title());
        //解析文件列表
        for(Element element : elements){
            String title = element.text();
            String url = webHost + element.select("a").attr("href");

            if(title.equals("$RECYCLE.BIN/")){
                continue;
            }

            if(title.equals("../")){
                fatherUrl = url;
                continue;
            }

            //分析文件夹还是文件
            if(title.endsWith("/")){
                //文件夹
                fileOrDirList.add(false);
            }else{
                fileOrDirList.add(true);

                //图片列表
                if(title.endsWith("png") || title.endsWith("jpg") || title.endsWith("bmp") || title.endsWith("gif") || title.endsWith("jpeg") || title.endsWith("ico")){
                    picItemUrlList.add(url);
                }
                //视频列表
                if(title.endsWith("avi") || title.endsWith("mov") || title.endsWith("mp4") || title.endsWith("mkv") || title.endsWith("wmv") || title.endsWith("flv") || title.endsWith("rmvb")){
                    //将当前文件夹所有的视频地址存入list
                    videoItemUrlList.add(url);
                    videoTitleList.add(url.substring(url.lastIndexOf("/") + 1,url.lastIndexOf(".")));
                }
                //文件类型列表
                fileTypeList.add(CheckFileType(url));
            }

            titList.add(title);
            urlList.add(url);

            MyRecRemoteFileInfos myRecRemoteFileInfos = new MyRecRemoteFileInfos();
            myRecRemoteFileInfos.setTitle(title);
            myRecRemoteFileInfos.setUrl(url);
            myRecRemoteFileInfosList.add(myRecRemoteFileInfos);
        }
        myRecRemoteFileAdapter.notifyDataSetChanged();
    }

    //查看文件
    private void showFile(int fileType,String fileUrl){
        switch(fileType) {
            case 0:
                //apk
                break;
            case 2:
                //xls
                break;
            case 3:
                //doc
                break;
            case 4:
                //ppt
                break;
            case 6:
                //ini
                break;
            case 7:
                //iso
                break;
            case 9:
                //pdf
                break;
            case 11:
                //txt
                break;
            case 12:
                //jar包
                break;
            case 13:
                //压缩包
                break;
            case 14:
                //大图
                clickPicIndex = picItemUrlList.indexOf(fileUrl);

                View viewPoP = LayoutInflater.from(RemoteFileActivity.this).inflate(R.layout.layout_fg_file_pop_bigpic,null);

                popupWindowPic = new PopupWindow(viewPoP, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                final ImageView leftPic = viewPoP.findViewById(R.id.iv_file_pop_bigpic_left);
                final ImageView rightPic = viewPoP.findViewById(R.id.iv_file_pop_bigpic_right);
                final PhotoView bigPicIV = viewPoP.findViewById(R.id.iv_file_pop_bigpic);
                ImageView bigPicIVClose = viewPoP.findViewById(R.id.iv_file_pop_bigpic_close);

                bigPicIVClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindowPic.dismiss();
                    }
                });

                leftPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickPicIndex = clickPicIndex - 1;

                        if(clickPicIndex >= 0){
                            rightPic.setVisibility(View.VISIBLE);
                            Picasso.get().load(picItemUrlList.get(clickPicIndex))
                                    .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                                    .placeholder(R.mipmap.loading_zhanwei)
                                    .error(R.mipmap.loading_faild)
                                    .into(bigPicIV);
                        }else{
                            clickPicIndex = 0;
                            leftPic.setVisibility(View.GONE);
                            Toast.makeText(RemoteFileActivity.this,"这是第一张呀",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                rightPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickPicIndex = clickPicIndex + 1;

                        if(clickPicIndex < picItemUrlList.size()){
                            leftPic.setVisibility(View.VISIBLE);
                            Picasso.get().load(picItemUrlList.get(clickPicIndex))
                                    .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                                    .placeholder(R.mipmap.loading_zhanwei)
                                    .error(R.mipmap.loading_faild)
                                    .into(bigPicIV);
                        }else{
                            clickPicIndex = clickPicIndex - 1;
                            rightPic.setVisibility(View.GONE);
                            Toast.makeText(RemoteFileActivity.this,"最后一张了",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                bigPicIV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        popupWindowPic.dismiss();
                        return true;
                    }
                });

                Picasso.get().load(fileUrl)
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .placeholder(R.mipmap.loading_zhanwei)
                        .error(R.mipmap.loading_faild)
                        .into(bigPicIV);

                popupWindowPic.showAtLocation(remote_file_parent, Gravity.CENTER,0,0);
                break;
            case 15:
                //视频
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RemoteFileActivity.this);
                boolean default_resolver_inner = sp.getBoolean("default_resolver_inner",true);

                if(default_resolver_inner){
                    //内部解析
                    Intent intent = new Intent(RemoteFileActivity.this,VideoPlayVTM.class);
                    intent.putExtra("videoType",0);
                    intent.putExtra("videoUrl",fileUrl);
                    intent.putExtra("videoTitle",fileUrl.substring((fileUrl.lastIndexOf("/") + 1),fileUrl.length()));
                    intent.putStringArrayListExtra("videoUrlList",videoItemUrlList);
                    intent.putStringArrayListExtra("videoTitleList",videoTitleList);
                    startActivity(intent);
                }else{
                    MyPhoneUtils.getInstance(RemoteFileActivity.this).usePlayerPlayNetVideo(fileUrl);
                }
                break;
            case 16:
                //磁链种子
                break;
            case 17:
                //音频
                break;
            default:
                //无自带解析器，调用其它解析器
                break;
        }
    }

    /**
     * 解析文件类型
     * @param fileUrl
     * @return
     */
    private int CheckFileType(String fileUrl){
        int label = 1;
        if(fileUrl.endsWith("png") || fileUrl.endsWith("jpg") || fileUrl.endsWith("bmp") || fileUrl.endsWith("gif") || fileUrl.endsWith("jpeg") || fileUrl.endsWith("ico")){
            label = 14;
        } else if(fileUrl.endsWith("apk")){
            label = 0;
        } else if(fileUrl.endsWith("xls") || fileUrl.endsWith("xlsx")){
            label = 2;
        } else if(fileUrl.endsWith("doc") || fileUrl.endsWith("docx")){
            label = 3;
        } else if(fileUrl.endsWith("ppt") || fileUrl.endsWith("pptx")){
            label = 4;
        } else if(fileUrl.endsWith("exe")){
            label = 5;
        } else if(fileUrl.endsWith("ini")){
            label = 6;
        } else if(fileUrl.endsWith("iso")){
            label = 7;
        } else if(fileUrl.endsWith("link")){
            label = 8;
        } else if(fileUrl.endsWith("pdf")){
            label = 9;
        } else if(fileUrl.endsWith("psd")){
            label = 10;
        } else if(fileUrl.endsWith("txt")){
            label = 11;
        } else if(fileUrl.endsWith("jar")){
            label = 12;
        } else if(fileUrl.endsWith("zip") || fileUrl.endsWith("rar") || fileUrl.endsWith("tar") || fileUrl.endsWith("7z")){
            label = 13;
        } else if(fileUrl.endsWith("avi") || fileUrl.endsWith("mov") || fileUrl.endsWith("mp4") || fileUrl.endsWith("mkv") || fileUrl.endsWith("wmv") || fileUrl.endsWith("flv") || fileUrl.endsWith("rmvb")){
            label = 15;
        } else if(fileUrl.endsWith("torrent")) {
            label = 16;
        }else if(fileUrl.endsWith("mp3") || fileUrl.endsWith("wma") || fileUrl.endsWith("wav")){
            label = 17;
        }else{
            label = 18;
        }
        return label;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if(popupWindowPic != null && popupWindowPic.isShowing()){
                popupWindowPic.dismiss();
            }else{
                if(canBack){
                    getRemoteSource(fatherUrl);
                }else{
                    finish();
                }
            }
        }
        return true;
    }
}
