package com.example.wgjrouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.database.LiveVideoList;
import com.example.utils.MyPhoneUtils;
import com.example.utils.MyVideoUtils;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

public class VideoPlayVTM extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, View.OnClickListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

    private FrameLayout vtmFLRoot;
    private VideoView videoView;

    private FrameLayout vtmFLTop;
    private ImageView vtmIVBack;
    private TextView vtmTVTitle;
    private TextView vtmTVSysTime;
    private ImageView vtmIVSet;
    private ImageView vtmIVFresh;
    private ImageView vtmIVCollect;

    private FrameLayout vtmFLCenter;
    private ImageView vtmIVBackward;
    private ImageView vtmIVForward;

    private FrameLayout vtmFLBottom;
    private SeekBar vtmSBar;
    private ImageView vtmIVPlay;
    private RelativeLayout lengthView;
    private TextView vtmTVVideoProgress;
    private TextView vtmTVVideoLength;
    private TextView vtmTVNetRate;
    private ImageView vtmIVListVideo;

    private ProgressBar vtmPBLoading;

    private TextView videoplay_layout_origin;
    private TextView videoplay_layout_scale;
    private TextView videoplay_layout_stretch;
    private TextView videoplay_layout_zoom;
    private TextView videoplay_quality_low;
    private TextView videoplay_quality_medium;
    private TextView videoplay_quality_high;
    private TextView videoplay_speed_0_5;
    private TextView videoplay_speed_0_8;
    private TextView videoplay_speed_1_0;
    private TextView videoplay_speed_1_2_5;
    private TextView videoplay_speed_1_5;
    private TextView videoplay_speed_2_0;

    //单击、双击判定
    int clickNum = 0;
    boolean isPlay = false;
    //标记路由文件、直播文件
    int videoType = 0;
    private String videoTit = null;
    private long TOUCH_SCREEN_TIME = 0;
    private static int SHOW_OPTION_DELAY = 6000;
    private long videoDuration = 0;

    //当前视频源地址
    private String curVideoUrl = "";
    private List<String> nameList = new ArrayList<>();
    private List<String> urlsList = new ArrayList<>();
    private int playItemPosition;

    private ArrayList<String> srcList = new ArrayList<>();
    private ArrayList<String> vTitleList = new ArrayList<>();

    private boolean isCollected = false;
    private PopupWindow pwVideoList;

    private SharedPreferences sp;

    private MediaPlayer mediaPlayer = null;
    private AudioManager audioManager;
    private int curVolume = 0;
    private int maxVolume = 0;
    //视频属性调整标记
    private int layoutFlag = 1;
    private int qualityFlag = 1;
    private int speedFlag = 2;

    private Handler clickHander = new Handler();
    private Handler hideHandler = new Handler(Looper.getMainLooper());
    private Handler progressTimeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 12138:
                    long currentPositionMesc = videoView.getCurrentPosition();
                    vtmTVVideoProgress.setText(MyVideoUtils.getInstance().formatVideoDuration(currentPositionMesc));
                    vtmSBar.setProgress((int)currentPositionMesc);
                    progressTimeHandler.sendEmptyMessageDelayed(12138,1000);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化维他命
        Vitamio.isInitialized(VideoPlayVTM.this);
        setContentView(R.layout.activity_videoplay);

        //初始化视图、配置
        initView();

        Intent getVideoUrl = getIntent();
        //0--路由器；1--直播
        videoType = getVideoUrl.getIntExtra("videoType",0);
        curVideoUrl = getVideoUrl.getStringExtra("videoUrl");
        videoTit = getVideoUrl.getStringExtra("videoTitle");
        playItemPosition = getVideoUrl.getIntExtra("videoPosition",0);

        Log.d("zouguo","videoType:" + videoType);
        Log.d("zouguo","curVideoUrl:" + curVideoUrl);
        Log.d("zouguo","videoTit:" + videoTit);
        Log.d("zouguo","playItemPosition:" + playItemPosition);

        switch (videoType){
            case 0:
                //路由器下视频
                //接收路由器硬盘目录下的视频列表
                srcList = getVideoUrl.getStringArrayListExtra("videoUrlList");
                vTitleList = getVideoUrl.getStringArrayListExtra("videoTitleList");

                vtmFLCenter.setVisibility(View.VISIBLE);
                vtmIVFresh.setVisibility(View.GONE);
                vtmIVCollect.setVisibility(View.GONE);
                break;
            case 1:
                //隐藏直播视频下不必要的控制组件
                vtmIVSet.setVisibility(View.GONE);
                vtmSBar.setVisibility(View.GONE);
                lengthView.setVisibility(View.GONE);
                vtmFLCenter.setVisibility(View.GONE);

                //加载播放源
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //收藏状态
                        LiveVideoList lvlc = LitePal.where("liveName = ? and liveUrls = ?",videoTit,curVideoUrl).findFirst(LiveVideoList.class);

                        if(lvlc.getIsCollect() == 1){
                            isCollected = true;
                        }

                        List<LiveVideoList> allVideoList = LitePal.findAll(LiveVideoList.class);

                        for(LiveVideoList lvl : allVideoList){
                            nameList.add(lvl.getLiveName());
                            urlsList.add(lvl.getLiveUrls());
                        }
                    }
                }).start();
                break;
            case 2:
                //隐藏直播视频下不必要的控制组件
                vtmIVSet.setVisibility(View.GONE);
                vtmSBar.setVisibility(View.GONE);
                lengthView.setVisibility(View.GONE);
                vtmFLCenter.setVisibility(View.GONE);

                //加载播放源
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //收藏状态
                        LiveVideoList lvlc = LitePal.where("liveName = ? and liveUrls = ?",videoTit,curVideoUrl).findFirst(LiveVideoList.class);

                        if(lvlc.getIsCollect() == 1){
                            isCollected = true;
                        }

                        List<LiveVideoList> allVideoList = LitePal.where("isplay = ?","1").find(LiveVideoList.class);

                        for(LiveVideoList lvl : allVideoList){
                            nameList.add(lvl.getLiveName());
                            urlsList.add(lvl.getLiveUrls());
                        }
                    }
                }).start();
                break;
            case 3:
                //隐藏直播视频下不必要的控制组件
                vtmIVSet.setVisibility(View.GONE);
                vtmSBar.setVisibility(View.GONE);
                lengthView.setVisibility(View.GONE);
                vtmFLCenter.setVisibility(View.GONE);

                //加载播放源
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //收藏状态
                        LiveVideoList lvlc = LitePal.where("liveName = ? and liveUrls = ?",videoTit,curVideoUrl).findFirst(LiveVideoList.class);

                        if(lvlc.getIsCollect() == 1){
                            isCollected = true;
                        }

                        List<LiveVideoList> allVideoList = LitePal.where("iscollect = ?","1").find(LiveVideoList.class);
                        for(LiveVideoList lvl : allVideoList){
                            nameList.add(lvl.getLiveName());
                            urlsList.add(lvl.getLiveUrls());
                        }
                    }
                }).start();
                break;
        }

        initSP();

        showControlView();

        playVideo(curVideoUrl);
    }

    private void showControlView(){
        if(isCollected){
            //收藏状态
            vtmIVCollect.setImageResource(R.mipmap.collected);
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd  HH:mm");
        vtmTVSysTime.setText(sdf.format(calendar.getTime()));

        vtmFLTop.setVisibility(View.VISIBLE);
        if(videoType == 0) {
            //路由文件
            vtmFLCenter.setVisibility(View.VISIBLE);
            vtmFLBottom.setVisibility(View.VISIBLE);
        }else{
            vtmFLCenter.setVisibility(View.GONE);
            vtmIVSet.setVisibility(View.GONE);
            vtmIVFresh.setVisibility(View.VISIBLE);
            vtmIVCollect.setVisibility(View.VISIBLE);
            vtmFLBottom.setVisibility(View.VISIBLE);
            vtmSBar.setVisibility(View.GONE);
            lengthView.setVisibility(View.GONE);
        }
    }

    private void hideControlView(){
        vtmFLTop.setVisibility(View.GONE);
        vtmFLCenter.setVisibility(View.GONE);
        vtmFLBottom.setVisibility(View.GONE);
    }

    private void initView(){
        vtmFLRoot = findViewById(R.id.vtmFLRoot);

        videoView = findViewById(R.id.vtmVideoView);

        vtmPBLoading = findViewById(R.id.vtmPBLoading);

        vtmFLTop = findViewById(R.id.vtmFLTop);
        vtmIVBack = findViewById(R.id.vtmIVBack);
        vtmTVTitle = findViewById(R.id.vtmTVTitle);
        vtmTVSysTime = findViewById(R.id.vtmTVSysTime);
        vtmIVSet = findViewById(R.id.vtmIVSet);
        vtmIVFresh = findViewById(R.id.vtmIVFresh);
        vtmIVCollect = findViewById(R.id.vtmIVCollect);

        vtmFLCenter = findViewById(R.id.vtmFLCenter);
        vtmIVBackward = findViewById(R.id.vtmIVBackward);
        vtmIVForward = findViewById(R.id.vtmIVForward);

        vtmFLBottom = findViewById(R.id.vtmFLBottom);
        vtmSBar = findViewById(R.id.vtmSBar);
        vtmIVPlay = findViewById(R.id.vtmIVPlay);
        lengthView = findViewById(R.id.lengthView);
        vtmTVVideoProgress = findViewById(R.id.vtmTVVideoProgress);
        vtmTVVideoLength = findViewById(R.id.vtmTVVideoLength);
        vtmTVNetRate = findViewById(R.id.vtmTVNetRate);
        vtmIVListVideo = findViewById(R.id.vtmIVListVideo);

        vtmIVSet.setOnClickListener(this);
        vtmIVFresh.setOnClickListener(this);
        vtmIVCollect.setOnClickListener(this);
        vtmFLRoot.setOnClickListener(this);
        vtmIVBack.setOnClickListener(this);
        vtmIVPlay.setOnClickListener(this);
        vtmIVListVideo.setOnClickListener(this);
        vtmIVBackward.setOnClickListener(this);
        vtmIVForward.setOnClickListener(this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        vtmSBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("zouguo","Touch:");
                TOUCH_SCREEN_TIME = System.currentTimeMillis();
                videoView.seekTo(seekBar.getProgress());
            }
        });
    }

    private void initSP(){
        //初始化一些参数
        sp = getSharedPreferences("app_setting",MODE_PRIVATE);
    }

    private void playVideo(String videoUrl){
        //检查网络类型
        if(MyPhoneUtils.getInstance(VideoPlayVTM.this).checkNetworkType() == 1){
            //流量
            Toast.makeText(VideoPlayVTM.this,"当前处于流量环境，请注意流量消耗",Toast.LENGTH_LONG).show();
        }

        if(videoTit != null){
            vtmTVTitle.setText(videoTit);
        }

        if(videoUrl != ""){
            videoView.setVideoPath(videoUrl);
            videoView.requestFocus();

            videoView.setOnPreparedListener(this);
            videoView.setOnErrorListener(this);
            videoView.setOnInfoListener(this);
            videoView.setOnBufferingUpdateListener(this);
            videoView.setOnCompletionListener(this);

            //设置暂停按钮
            vtmIVPlay.setImageResource(R.mipmap.pausevideo);
            isPlay = true;
        }else{
            Toast.makeText(this,"出错:视频地址异常",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0){
                    if(pwVideoList != null && pwVideoList.isShowing()){
                        pwVideoList.dismiss();
                        return true;
                    }

                    //记录当前播放位置
                    videoView.stopPlayback();
                    if(videoType == 0)
                        //微路由文件
                        sp.edit().putLong("vitamio_play_position",videoView.getCurrentPosition()).commit();

                    Intent data = new Intent();
                    data.putExtra("videoPosition",playItemPosition);
                    setResult(RESULT_OK,data);
                    finish();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(VideoPlayVTM.this);
                boolean setLimitVol = sp.getBoolean("protect_ear_volume",false);

                curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1;

                if(setLimitVol){
                    //设置了音量保护
                    int limitVol = Integer.parseInt(sp.getString("ideal_ear_volume","100"));

                    limitVol = Integer.parseInt(String.valueOf(Math.round(limitVol / 100.0 * maxVolume)));

                    if(curVolume > limitVol){
                        Toast.makeText(VideoPlayVTM.this,"当前开启了音量保护，无法继续",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }else{
                    if(curVolume > maxVolume){
                        return true;
                    }
                }

                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(curVolume != -1){
                    curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1;
                    if(curVolume < 0){
                        curVolume = 0;
                    }

                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
                }
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if(videoType == 0) {
            //微路由文件
            String tlength = MyVideoUtils.getInstance().formatVideoDuration(videoView.getDuration());

            vtmTVVideoLength.setText(tlength);
            videoDuration = videoView.getDuration();
            vtmSBar.setMax((int)videoDuration);
        }

        videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        this.mediaPlayer = mediaPlayer;
        mediaPlayer.setPlaybackSpeed(1.0f);
        mediaPlayer.start();

        vtmIVPlay.setImageResource(R.mipmap.pausevideo);
        isPlay = true;

        //MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
        //Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(VideoPlayVTM.this,curVideoUrl, MediaStore.Video.Thumbnails.MINI_KIND);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Toast.makeText(this,"视频链接无效",Toast.LENGTH_SHORT).show();

        vtmFLTop.setVisibility(View.VISIBLE);
        vtmFLBottom.setVisibility(View.VISIBLE);
        if(videoType != 0) {
            //路由文件
            vtmFLBottom.setVisibility(View.VISIBLE);
            vtmIVSet.setVisibility(View.GONE);
            vtmIVFresh.setVisibility(View.VISIBLE);
            vtmIVCollect.setVisibility(View.VISIBLE);
            vtmSBar.setVisibility(View.GONE);
            lengthView.setVisibility(View.GONE);
        }

        vtmIVPlay.setImageResource(R.mipmap.playvideo);
        isPlay = false;

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        switch(what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d("zouguo","开始缓冲");
                if(videoType == 0){
                    //直播视频不显示进度条
                    vtmPBLoading.setVisibility(View.VISIBLE);
                }
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                int rate = extra;
                String unit = "Kb/s";
                if(rate > 1000){
                    rate = rate / 1000;
                    unit = "Mb/s";
                }
                vtmTVNetRate.setText(String.valueOf(rate) + unit);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d("zouguo","缓冲结束");
                vtmPBLoading.setVisibility(View.GONE);

                if(System.currentTimeMillis() - TOUCH_SCREEN_TIME > SHOW_OPTION_DELAY){
                    hideControlView();
                }
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        Log.d("zouguo","percent:" + percent);
        if(percent == 0 &&!videoView.isPlaying()){
            vtmPBLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(videoType == 0) {
            vtmFLTop.setVisibility(View.VISIBLE);
            vtmFLBottom.setVisibility(View.VISIBLE);
            vtmPBLoading.setVisibility(View.GONE);

            //微路由文件,回到第一帧
            videoView.seekTo(0);

            vtmIVPlay.setImageResource(R.mipmap.playvideo);
            isPlay = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("zouguo","onStart");
        if(videoType == 0) {
            //微路由文件
            progressTimeHandler.sendEmptyMessage(12138);
            long seekToLast = sp.getLong("vitamio_play_position",0);
            videoView.seekTo(seekToLast);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //恢复播放
        videoView.resume();
        Log.d("zouguo","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //记录播放位置
        videoView.pause();
        if(videoType == 0)
            //微路由文件,记录播放位置
            sp.edit().putLong("vitamio_play_position",videoView.getCurrentPosition()).commit();
        Log.d("zouguo","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("zouguo","onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("zouguo","onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        if(videoType == 0)
            //微路由文件
            progressTimeHandler.removeMessages(12138);
        Log.d("zouguo","onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.vtmFLRoot:
                //判断单击、双击
                clickNum++;
                clickHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(clickNum == 1){
                            //单击
                            if(vtmFLTop.isShown()){
                                vtmFLTop.setVisibility(View.GONE);
                                vtmFLCenter.setVisibility(View.GONE);
                                vtmFLBottom.setVisibility(View.GONE);
                            }else{
                                showControlView();
                                TOUCH_SCREEN_TIME = System.currentTimeMillis();
                            }
                        }else if(clickNum == 2){
                            //双击
                            if(videoView.isPlaying()){
                                videoView.pause();
                                vtmIVPlay.setImageResource(R.mipmap.playvideo);
                                isPlay = false;

                                showControlView();
                            }else{
                                videoView.start();
                                vtmIVPlay.setImageResource(R.mipmap.pausevideo);
                                isPlay = true;

                                hideHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(isPlay){
                                            //点击暂停播放的时候，不隐去
                                            hideControlView();
                                        }
                                    }
                                },SHOW_OPTION_DELAY);
                            }
                        }
                        clickHander.removeCallbacksAndMessages(null);
                        clickNum = 0;
                    }
                },300);
                break;
            case R.id.vtmIVBack:
                videoView.stopPlayback();

                Intent data = new Intent();
                data.putExtra("videoPosition",playItemPosition);
                setResult(RESULT_OK,data);
                finish();
                break;
            case R.id.vtmIVSet:
                View view = View.inflate(VideoPlayVTM.this,R.layout.dialog_videoplay_setting,null);

                PopupWindow popupWindow = new PopupWindow(view,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);

                videoplay_layout_origin = view.findViewById(R.id.videoplay_layout_origin);
                videoplay_layout_scale = view.findViewById(R.id.videoplay_layout_scale);
                videoplay_layout_stretch = view.findViewById(R.id.videoplay_layout_stretch);
                videoplay_layout_zoom = view.findViewById(R.id.videoplay_layout_zoom);
                videoplay_quality_low = view.findViewById(R.id.videoplay_quality_low);
                videoplay_quality_medium = view.findViewById(R.id.videoplay_quality_medium);
                videoplay_quality_high = view.findViewById(R.id.videoplay_quality_high);
                videoplay_speed_0_5 = view.findViewById(R.id.videoplay_speed_0_5);
                videoplay_speed_0_8 = view.findViewById(R.id.videoplay_speed_0_8);
                videoplay_speed_1_0 = view.findViewById(R.id.videoplay_speed_1_0);
                videoplay_speed_1_2_5 = view.findViewById(R.id.videoplay_speed_1_2_5);
                videoplay_speed_1_5 = view.findViewById(R.id.videoplay_speed_1_5);
                videoplay_speed_2_0 = view.findViewById(R.id.videoplay_speed_2_0);

                switch (layoutFlag){
                    case 0:
                        videoplay_layout_origin.setTextColor(Color.RED);
                        videoplay_layout_scale.setTextColor(Color.WHITE);
                        videoplay_layout_stretch.setTextColor(Color.WHITE);
                        videoplay_layout_zoom.setTextColor(Color.WHITE);
                        break;
                    case 1:
                        videoplay_layout_origin.setTextColor(Color.WHITE);
                        videoplay_layout_scale.setTextColor(Color.RED);
                        videoplay_layout_stretch.setTextColor(Color.WHITE);
                        videoplay_layout_zoom.setTextColor(Color.WHITE);
                        break;
                    case 2:
                        videoplay_layout_origin.setTextColor(Color.WHITE);
                        videoplay_layout_scale.setTextColor(Color.WHITE);
                        videoplay_layout_stretch.setTextColor(Color.RED);
                        videoplay_layout_zoom.setTextColor(Color.WHITE);
                        break;
                    case 3:
                        videoplay_layout_origin.setTextColor(Color.WHITE);
                        videoplay_layout_scale.setTextColor(Color.WHITE);
                        videoplay_layout_stretch.setTextColor(Color.WHITE);
                        videoplay_layout_zoom.setTextColor(Color.RED);
                        break;
                }

                switch (qualityFlag){
                    case 0:
                        videoplay_quality_low.setTextColor(Color.RED);
                        videoplay_quality_medium.setTextColor(Color.WHITE);
                        videoplay_quality_high.setTextColor(Color.WHITE);
                        break;
                    case 1:
                        videoplay_quality_low.setTextColor(Color.WHITE);
                        videoplay_quality_medium.setTextColor(Color.RED);
                        videoplay_quality_high.setTextColor(Color.WHITE);
                        break;
                    case 2:
                        videoplay_quality_low.setTextColor(Color.WHITE);
                        videoplay_quality_medium.setTextColor(Color.WHITE);
                        videoplay_quality_high.setTextColor(Color.RED);
                        break;
                }

                switch (speedFlag){
                    case 0:
                        videoplay_speed_0_5.setTextColor(Color.RED);
                        videoplay_speed_0_8.setTextColor(Color.WHITE);
                        videoplay_speed_1_0.setTextColor(Color.WHITE);
                        videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                        videoplay_speed_1_5.setTextColor(Color.WHITE);
                        videoplay_speed_2_0.setTextColor(Color.WHITE);
                        break;
                    case 1:
                        videoplay_speed_0_5.setTextColor(Color.WHITE);
                        videoplay_speed_0_8.setTextColor(Color.RED);
                        videoplay_speed_1_0.setTextColor(Color.WHITE);
                        videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                        videoplay_speed_1_5.setTextColor(Color.WHITE);
                        videoplay_speed_2_0.setTextColor(Color.WHITE);
                        break;
                    case 2:
                        videoplay_speed_0_5.setTextColor(Color.WHITE);
                        videoplay_speed_0_8.setTextColor(Color.WHITE);
                        videoplay_speed_1_0.setTextColor(Color.RED);
                        videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                        videoplay_speed_1_5.setTextColor(Color.WHITE);
                        videoplay_speed_2_0.setTextColor(Color.WHITE);
                        break;
                    case 3:
                        videoplay_speed_0_5.setTextColor(Color.WHITE);
                        videoplay_speed_0_8.setTextColor(Color.WHITE);
                        videoplay_speed_1_0.setTextColor(Color.WHITE);
                        videoplay_speed_1_2_5.setTextColor(Color.RED);
                        videoplay_speed_1_5.setTextColor(Color.WHITE);
                        videoplay_speed_2_0.setTextColor(Color.WHITE);
                        break;
                    case 4:
                        videoplay_speed_0_5.setTextColor(Color.WHITE);
                        videoplay_speed_0_8.setTextColor(Color.WHITE);
                        videoplay_speed_1_0.setTextColor(Color.WHITE);
                        videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                        videoplay_speed_1_5.setTextColor(Color.RED);
                        videoplay_speed_2_0.setTextColor(Color.WHITE);
                        break;
                    case 5:
                        videoplay_speed_0_5.setTextColor(Color.WHITE);
                        videoplay_speed_0_8.setTextColor(Color.WHITE);
                        videoplay_speed_1_0.setTextColor(Color.WHITE);
                        videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                        videoplay_speed_1_5.setTextColor(Color.WHITE);
                        videoplay_speed_2_0.setTextColor(Color.RED);
                        break;
                }

                videoplay_layout_origin.setOnClickListener(this);
                videoplay_layout_scale.setOnClickListener(this);
                videoplay_layout_stretch.setOnClickListener(this);
                videoplay_layout_zoom.setOnClickListener(this);
                videoplay_quality_low.setOnClickListener(this);
                videoplay_quality_medium.setOnClickListener(this);
                videoplay_quality_high.setOnClickListener(this);

                videoplay_speed_0_5.setOnClickListener(this);
                videoplay_speed_0_8.setOnClickListener(this);
                videoplay_speed_1_0.setOnClickListener(this);
                videoplay_speed_1_2_5.setOnClickListener(this);
                videoplay_speed_1_5.setOnClickListener(this);
                videoplay_speed_2_0.setOnClickListener(this);

                ColorDrawable cd = new ColorDrawable(0xb0000000);
                popupWindow.setBackgroundDrawable(cd);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(videoView, Gravity.RIGHT,0,0);
                break;
            case R.id.vtmIVFresh:
                videoView.setVideoPath(curVideoUrl);
                break;
            case R.id.vtmIVCollect:
                LiveVideoList lvl = LitePal.where("liveName = ? and liveUrls = ?",videoTit,curVideoUrl).findFirst(LiveVideoList.class);

                if(isCollected){
                    lvl.setIsCollect(0);

                    //取消收藏
                    isCollected = false;
                    vtmIVCollect.setImageResource(R.mipmap.collect);
                }else{
                    lvl.setIsCollect(1);

                    //添加收藏
                    isCollected = true;
                    vtmIVCollect.setImageResource(R.mipmap.collected);
                }

                lvl.save();
                break;
            case R.id.vtmIVBackward:
                Log.d("zouguo","Current1:" + videoView.getCurrentPosition());
                TOUCH_SCREEN_TIME = System.currentTimeMillis();

                long backwardTo = videoView.getCurrentPosition() - 10 * 1000;//回退5秒
                if(backwardTo < 0){
                    backwardTo = 0;
                }
                videoView.seekTo(backwardTo);
                Log.d("zouguo","Current2:" + videoView.getCurrentPosition());
                break;
            case R.id.vtmIVForward:
                Log.d("zouguo","Current3:" + videoView.getCurrentPosition());
                TOUCH_SCREEN_TIME = System.currentTimeMillis();

                long forwardTo = videoView.getCurrentPosition() + 10 * 1000;//快进5秒
                if(forwardTo > videoView.getDuration()){
                    forwardTo = videoView.getDuration() - 5 * 1000;//防止直接到终点
                }
                videoView.seekTo(forwardTo);
                Log.d("zouguo","Current4:" + videoView.getCurrentPosition());
                break;
            case R.id.vtmIVPlay:
                if(videoView.isPlaying()){
                    videoView.pause();
                    vtmIVPlay.setImageResource(R.mipmap.playvideo);
                    isPlay = false;
                    return;
                }
                videoView.start();
                vtmIVPlay.setImageResource(R.mipmap.pausevideo);
                isPlay = true;
                break;
            case R.id.vtmIVListVideo:
                View videolist = View.inflate(VideoPlayVTM.this,R.layout.dialog_videoplay_list,null);

                TextView list_video_nums_tv = videolist.findViewById(R.id.list_video_nums_tv);
                ListView list_video_lv = videolist.findViewById(R.id.list_video_lv);

                if(videoType == 0){
                    list_video_nums_tv.setText("视频：" + String.valueOf(vTitleList.size()));
                    ArrayAdapter<String> aa = new ArrayAdapter<String>(this,R.layout.item_lv_listvideo_white,vTitleList);
                    list_video_lv.setAdapter(aa);
                }else{
                    list_video_nums_tv.setText("直播：" + String.valueOf(nameList.size()));
                    ArrayAdapter<String> aa = new ArrayAdapter<String>(this,R.layout.item_lv_listvideo_white,nameList);
                    list_video_lv.setAdapter(aa);
                }

                list_video_lv.setSelection(playItemPosition);

                list_video_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        playItemPosition = position;

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd  HH:mm");
                        vtmTVSysTime.setText(sdf.format(calendar.getTime()));

                        vtmFLTop.setVisibility(View.VISIBLE);
                        vtmFLBottom.setVisibility(View.VISIBLE);
                        if(videoType != 0) {
                            vtmIVSet.setVisibility(View.GONE);
                            vtmFLBottom.setVisibility(View.VISIBLE);
                            vtmSBar.setVisibility(View.GONE);
                            lengthView.setVisibility(View.GONE);
                        }

                        if(videoType == 0){
                            curVideoUrl = srcList.get(position);
                            vtmTVTitle.setText(vTitleList.get(position));
                        }else{
                            curVideoUrl = urlsList.get(position);
                            vtmTVTitle.setText(nameList.get(position));
                        }

                        vtmTVVideoLength.setText("读取中...");
                        videoView.setVideoPath(curVideoUrl);

                        hideHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(isPlay){
                                    //点击暂停播放的时候，不隐去
                                    hideControlView();
                                }
                            }
                        },SHOW_OPTION_DELAY);
                    }
                });

                pwVideoList = new PopupWindow(videolist,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);

                ColorDrawable df = new ColorDrawable(0xb0000000);
                pwVideoList.setBackgroundDrawable(df);
                pwVideoList.setTouchable(true);
                pwVideoList.setOutsideTouchable(true);
                pwVideoList.showAtLocation(videoView,Gravity.RIGHT,0,0);
                break;
            case R.id.videoplay_layout_origin:
                //视频--原始画面
                videoplay_layout_origin.setTextColor(Color.RED);
                videoplay_layout_scale.setTextColor(Color.WHITE);
                videoplay_layout_stretch.setTextColor(Color.WHITE);
                videoplay_layout_zoom.setTextColor(Color.WHITE);
                videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ORIGIN,0);

                layoutFlag = 0;
                break;
            case R.id.videoplay_layout_scale:
                //视频--全屏画面
                videoplay_layout_origin.setTextColor(Color.WHITE);
                videoplay_layout_scale.setTextColor(Color.RED);
                videoplay_layout_stretch.setTextColor(Color.WHITE);
                videoplay_layout_zoom.setTextColor(Color.WHITE);
                videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE,0);

                layoutFlag = 1;
                break;
            case R.id.videoplay_layout_stretch:
                //视频--拉伸画面
                videoplay_layout_origin.setTextColor(Color.WHITE);
                videoplay_layout_scale.setTextColor(Color.WHITE);
                videoplay_layout_stretch.setTextColor(Color.RED);
                videoplay_layout_zoom.setTextColor(Color.WHITE);
                videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH,0);

                layoutFlag = 2;
                break;
            case R.id.videoplay_layout_zoom:
                //视频--裁剪画面
                videoplay_layout_origin.setTextColor(Color.WHITE);
                videoplay_layout_scale.setTextColor(Color.WHITE);
                videoplay_layout_stretch.setTextColor(Color.WHITE);
                videoplay_layout_zoom.setTextColor(Color.RED);
                videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM,0);

                layoutFlag = 3;
                break;
            case R.id.videoplay_quality_low:
                //视频--流畅
                videoplay_quality_low.setTextColor(Color.RED);
                videoplay_quality_medium.setTextColor(Color.WHITE);
                videoplay_quality_high.setTextColor(Color.WHITE);
                videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_LOW);

                qualityFlag = 0;
                break;
            case R.id.videoplay_quality_medium:
                //视频--普通
                videoplay_quality_low.setTextColor(Color.WHITE);
                videoplay_quality_medium.setTextColor(Color.RED);
                videoplay_quality_high.setTextColor(Color.WHITE);
                videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);

                qualityFlag = 1;
                break;
            case R.id.videoplay_quality_high:
                //视频--高质
                videoplay_quality_low.setTextColor(Color.WHITE);
                videoplay_quality_medium.setTextColor(Color.WHITE);
                videoplay_quality_high.setTextColor(Color.RED);
                videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);

                qualityFlag = 2;
                break;
            case R.id.videoplay_speed_0_5:
                //视频--0.5速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.RED);
                    videoplay_speed_0_8.setTextColor(Color.WHITE);
                    videoplay_speed_1_0.setTextColor(Color.WHITE);
                    videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                    videoplay_speed_1_5.setTextColor(Color.WHITE);
                    videoplay_speed_2_0.setTextColor(Color.WHITE);

                    mediaPlayer.setPlaybackSpeed(0.5f);

                    speedFlag = 0;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.videoplay_speed_0_8:
                //视频--0.8速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.WHITE);
                    videoplay_speed_0_8.setTextColor(Color.RED);
                    videoplay_speed_1_0.setTextColor(Color.WHITE);
                    videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                    videoplay_speed_1_5.setTextColor(Color.WHITE);
                    videoplay_speed_2_0.setTextColor(Color.WHITE);

                    mediaPlayer.setPlaybackSpeed(0.8f);

                    speedFlag = 1;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videoplay_speed_1_0:
                //视频--1倍速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.WHITE);
                    videoplay_speed_0_8.setTextColor(Color.WHITE);
                    videoplay_speed_1_0.setTextColor(Color.RED);
                    videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                    videoplay_speed_1_5.setTextColor(Color.WHITE);
                    videoplay_speed_2_0.setTextColor(Color.WHITE);

                    mediaPlayer.setPlaybackSpeed(1.0f);

                    speedFlag = 2;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videoplay_speed_1_2_5:
                //视频--1.25速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.WHITE);
                    videoplay_speed_0_8.setTextColor(Color.WHITE);
                    videoplay_speed_1_0.setTextColor(Color.WHITE);
                    videoplay_speed_1_2_5.setTextColor(Color.RED);
                    videoplay_speed_1_5.setTextColor(Color.WHITE);
                    videoplay_speed_2_0.setTextColor(Color.WHITE);

                    mediaPlayer.setPlaybackSpeed(1.25f);

                    speedFlag = 3;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videoplay_speed_1_5:
                //视频--1.5速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.WHITE);
                    videoplay_speed_0_8.setTextColor(Color.WHITE);
                    videoplay_speed_1_0.setTextColor(Color.WHITE);
                    videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                    videoplay_speed_1_5.setTextColor(Color.RED);
                    videoplay_speed_2_0.setTextColor(Color.WHITE);

                    mediaPlayer.setPlaybackSpeed(1.5f);

                    speedFlag = 4;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videoplay_speed_2_0:
                //视频--2.0速
                if(mediaPlayer != null){
                    videoplay_speed_0_5.setTextColor(Color.WHITE);
                    videoplay_speed_0_8.setTextColor(Color.WHITE);
                    videoplay_speed_1_0.setTextColor(Color.WHITE);
                    videoplay_speed_1_2_5.setTextColor(Color.WHITE);
                    videoplay_speed_1_5.setTextColor(Color.WHITE);
                    videoplay_speed_2_0.setTextColor(Color.RED);

                    mediaPlayer.setPlaybackSpeed(2.0f);

                    speedFlag = 5;
                }else{
                    Toast.makeText(VideoPlayVTM.this,"抱歉，媒体播放器尚未初始化",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
