package com.example.wgjrouter;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MyRecFilesAdapter;
import com.example.bean.MyRecFileInfos;
import com.example.bean.SmbFileInfos;
import com.example.consts.Consts;
import com.example.event.MessageEvent;
import com.example.samba.SambaUtils;
import com.example.utils.MyDateUtils;
import com.example.utils.MyFileUtils;
import com.example.utils.MyPhoneUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FragmentFile extends Fragment{

    Gson gson = new Gson();

    private SwipeRefreshLayout fg_file_sr;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private PopupWindow popupWindowPic;
    private View fg_file_opendir_cl;

    private List<MyRecFileInfos> myFgLvInfosList;
    private MyRecFilesAdapter myFgLvArrayAdapter;
    private LinearLayout linearLayoutSearch;
    private EditText editTextSearch;
    private Button buttonSearch;
    private ProgressBar fg_file_pb;
    private ImageView fg_file_error_iv;
    private View fg_file_showpic_label;//图片弹窗显示/隐藏标记位

    /**
     * 0--Holl
     * 1--OpenWrt   默认
     */
    private int routerType = 0;
    private String routerHost = "";
    private String canoniUrl = "";//随着目录的进入和回退而变化
    private String rootUrl = "";//固定
    private String routerName = "";
    private boolean waitChangeTitle = false;

    SharedPreferences sharedPreferences = null;
    private boolean isSearch = false;
    private boolean transCoverFile = true;//默认传输时覆盖目标文件夹已有的文件

    //读取配置的WiFi名称关键字、网关、软链接名、挂载类型
    private boolean wifi_default_config = true;
    private String gateway_config;
    private String samba_config;
    private String link_diskdir_config;

    private String jsonFileDir = "";
    private String toolbarTile = "";

    //文件属性
    private List<String> fileInfosList = new ArrayList<>();
    //上层目录
    private List<String> parentTitBack = new ArrayList<>();
    private List<String> parentUrlList = new ArrayList<>();
    private List<String> parentUrlBack = new ArrayList<>();
    private List<Boolean> fileOrDirList = new ArrayList<>();//标记当前Item是文件、文件夹
    private List<String> fileTitleList = new ArrayList<>();
    private List<Integer> fileTypeList = new ArrayList<>();//标记文件的类型，下载用
    private int fileType = 0;//标记本地文件类型，上传用
    private List<String> canoniUlrList = new ArrayList<>();
    private List<String> picItemUrlList = new ArrayList<>();
    private ArrayList<String> videoTitleList = new ArrayList<>();
    private ArrayList<String> videoItemUrlList = new ArrayList<>();

    private int dirCounts = 0;
    private int fileCounts = 0;

    private View parentView = null;
    private boolean isFresh = false;
    private boolean isShowPic = false;
    private int clickPicIndex = 0;
    private int longClickItemIndex = 0;
    private String newFolderName = null;
    private int newFolderIndex = 0;

    private int FILE_REQUEST_CODE = 121;
    private int REQUEST_PERMISSION_CODE = 9737;

    private AlertDialog alertDialog = null;
    private static String CHANNEL_ID = "channel_wgj";
    private static String CHANNEL_NAME = "channel_wgjrouter";
    private Notification.Builder nb;
    private NotificationCompat.Builder ncb;
    private Notification notification;
    private NotificationManager notificationManager = null;

    String urlPath = null;
    String newfname = null;

    private boolean isEdit = false;
    private EventBus eventBus;
    //网络变化接收广播
    private NetworkChangeReceive networkChangeReceive = null;

    private Handler handler = new Handler();

    private Handler hanlderFile = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 122:
                case 123:
                case 125:
                case 127:
                case 129:
                    Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 126:
                    //文件已存在
                    final String fileName = msg.obj.toString();
                    final int fileType = msg.arg1;
                    android.app.AlertDialog ad = null;
                    android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(getActivity());
                    adb.setTitle("上传提醒");
                    adb.setMessage("当前文件已存在，上传将覆盖，是否继续");
                    adb.setNegativeButton("算了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    adb.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            eventBus.postSticky(new MessageEvent(2,fileType,fileName,0,canoniUrl,urlPath));
                        }
                    });
                    ad = adb.create();
                    ad.show();
                    ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                    ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                    break;
                case 128:
                    List<Integer> listSize = new ArrayList<>();
                    listSize = (List<Integer>) msg.obj;

                    if(listSize.size() > 0){
                        //获取成功
                        AlertDialog adf = null;
                        AlertDialog.Builder adbf = new AlertDialog.Builder(getActivity());
                        adbf.setTitle(fileTitleList.get(msg.arg1));
                        adbf.setMessage("子项数：" + listSize.get(0) + "\n文件数：" + String.valueOf(listSize.get(1) + "\n文件夹数：" + String.valueOf(listSize.get(2))));
                        adbf.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        adf = adbf.create();
                        adf.show();
                    }else{
                        Toast.makeText(getActivity(),"当前文件夹为空",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 130:
                    Toast.makeText(getActivity(),"重命名成功，刷新中...",Toast.LENGTH_SHORT).show();

                    fresh(canoniUrl);
                    break;
                case 131:
                    Toast.makeText(getActivity(),"删除成功，刷新中...",Toast.LENGTH_SHORT).show();

                    fresh(canoniUrl);
                    break;
                case 132:
                    //删除进度对话框更新
                    TextView progress_tv = alertDialog.findViewById(R.id.dialog_waitting_schedule_tv);

                    if(msg.arg1 == 0){
                        progress_tv.setText(msg.obj.toString());
                    }else{
                        alertDialog.dismiss();
                        hanlderFile.removeMessages(132);

                        Toast.makeText(getActivity(),"删除成功，刷新中...",Toast.LENGTH_SHORT).show();

                        fresh(canoniUrl);
                    }

                    if(!alertDialog.isShowing()){
                        hanlderFile.removeMessages(132);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        //注册网络变化广播接收器
        networkChangeReceive = new NetworkChangeReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(networkChangeReceive,intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.layout_fg_file,container,false);

        //初始化一些参数
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        transCoverFile = sharedPreferences.getBoolean("override_destfile",true);//个性化配置菜单：是否覆盖下载

        initView(parentView);

        //刷新数据
        fresh(canoniUrl);
        return parentView;
    }

    class NetworkChangeReceive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(MyPhoneUtils.getInstance(getActivity()).checkNetworkState() && MyPhoneUtils.getInstance(getActivity()).checkNetworkType() == 2){
                /**
                 * 网络状态有变化，并且网络为连接，切换WIFI
                 * 刷新数据
                 */
                fresh(canoniUrl);
            }
        }
    }

    //初始化视图
    private void initView(final View parentView){
        fg_file_showpic_label = parentView.findViewById(R.id.fg_file_showpic_label);
        fg_file_sr = parentView.findViewById(R.id.fg_file_sr);
        fg_file_pb = parentView.findViewById(R.id.fg_file_pb);
        fg_file_error_iv = parentView.findViewById(R.id.fg_file_error_iv);
        recyclerView = parentView.findViewById(R.id.fg_file_recyclerview);
        toolbar = parentView.findViewById(R.id.fg_file_toolbar);
        linearLayoutSearch = parentView.findViewById(R.id.fg_file_ll_search);
        editTextSearch = parentView.findViewById(R.id.fg_file_et_search);
        buttonSearch = parentView.findViewById(R.id.fg_file_btn_search);
        fg_file_opendir_cl = parentView.findViewById(R.id.fg_file_opendir_cl);

        setHasOptionsMenu(true);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);

        if(canoniUrl.equals("")){
            //读取配置数据
            wifi_default_config = sharedPreferences.getBoolean("wifi_default_config",true);
            gateway_config = sharedPreferences.getString("gateway_config","");
            samba_config = sharedPreferences.getString("samba_config","");
            link_diskdir_config = sharedPreferences.getString("link_diskdir_config","");

            if(wifi_default_config){
                routerName = "Holl微路由";
                //默认路由为Holl
                routerType = 0;
                routerHost = Consts.NET_HOST_HOLL;
                rootUrl = Consts.ROOT_URL_HOLL;
                canoniUrl = Consts.CANONI_URL_HOLL;
            }else{
                routerName = "智能路由器";
                if(gateway_config != "" && samba_config != "" && link_diskdir_config != "" ){
                    routerType = 1;
                    routerHost = "http://" + gateway_config + "/" + link_diskdir_config + "/";
                    rootUrl = "smb://" + samba_config + "@" + gateway_config + "/";
                    canoniUrl = "smb://" + samba_config + "@" + gateway_config + "/";
                }else{
                    Toast.makeText(getActivity(),"智能路由器未初始化配置",Toast.LENGTH_SHORT).show();
                }
            }
        }

        fg_file_error_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fresh(canoniUrl);
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        myFgLvInfosList = new ArrayList<>();
        myFgLvArrayAdapter = new MyRecFilesAdapter(getActivity(),eventBus,routerType,routerHost,myFgLvInfosList);
        recyclerView.setAdapter(myFgLvArrayAdapter);

        myFgLvArrayAdapter.setOnItemListener(new MyRecFilesAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(myFgLvArrayAdapter.getMultiChoiceMode() == 1){
                    //此时在多选状态，项目选择状态翻转
                    CheckBox cb = view.findViewById(R.id.item_cb_fileselect);

                    if(cb.isChecked()){
                        myFgLvArrayAdapter.notifyItemChanged(position,false);
                    }else{
                        myFgLvArrayAdapter.notifyItemChanged(position,true);
                    }
                    return;
                }

                //加载完成前不允许再点击
                /**
                 * 判断项目类型
                 * 1.文件夹--进入文件夹
                 * 2.图片、办公文件--调用自带查看器
                 * 3.其它--调用系统查看器
                 */
                if(!fileOrDirList.get(position)){
                    //文件夹
                    toolbarTile = fileTitleList.get(position);
                    toolbar.setTitle(toolbarTile);
                    toolbar.setNavigationIcon(R.mipmap.back);

                    parentTitBack.add(fileTitleList.get(position));
                    parentUrlBack.add(parentUrlList.get(position));
                    canoniUrl = canoniUrl + fileTitleList.get(position);

                    fresh(canoniUrl);
                }else{
                    //分析文件类型
                    boolean isFile = fileOrDirList.get(position);

                    //Samba链接转真实链接
                    String httpSrcUrl = "";
                    switch (routerType){
                        case 0:
                            httpSrcUrl = canoniUlrList.get(position).substring((canoniUlrList.get(position).indexOf("/holl/") + 5),canoniUlrList.get(position).length());
                            break;
                        case 1:
                            httpSrcUrl = canoniUlrList.get(position).substring((canoniUlrList.get(position).indexOf("@192.168.80.1/") + 14),canoniUlrList.get(position).length());
                            break;
                    }

                    httpSrcUrl = routerHost + httpSrcUrl;

                    String fileUrl = canoniUlrList.get(position).toLowerCase();

                    int label = MyFileUtils.getInstance().checkFileType(fileUrl);

                    showFile(label,httpSrcUrl);
                }
            }

            @Override
            public void onItemLongClick(View view, final int parentPosition) {
                if(myFgLvArrayAdapter.getMultiChoiceMode() == 1){
                    //此时在多选状态
                    return;
                }

                longClickItemIndex = parentPosition;

                View viewPoP = View.inflate(getActivity(),R.layout.dialog_file_longclick_menu,null);

                ListView listView = viewPoP.findViewById(R.id.dialog_file_longclick_menu_lv);

                String[] menuData = {"删除","下载","分析","重命名","取消"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,menuData);
                listView.setAdapter(arrayAdapter);

                final PopupWindow popupWindow = new PopupWindow(viewPoP,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                        popupWindow.dismiss();

                        switch (position){
                            case 0:
                                //删除
                                if(canoniUrl.equals(rootUrl)){
                                    Toast.makeText(getActivity(),"抱歉,根目录不允许此操作",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                AlertDialog ad = null;
                                final AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                                adb.setTitle("请确认是否删除:");
                                adb.setMessage(fileTitleList.get(parentPosition));
                                adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //删除当前文件夹
                                                if(SambaUtils.getInstance(waitChangeTitle).deleteItem(canoniUrl,fileTitleList.get(parentPosition))){
                                                    //刷新
                                                    Message msg = new Message();
                                                    msg.what = 131;
                                                    hanlderFile.sendMessage(msg);
                                                }else{
                                                    Message msg = new Message();
                                                    msg.what = 125;
                                                    msg.obj = "抱歉，删除文件夹失败";
                                                    hanlderFile.sendMessage(msg);
                                                }
                                            }
                                        }).start();
                                    }
                                });
                                adb.setNegativeButton("算了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                ad = adb.create();
                                ad.show();
                                ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                                ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                                break;
                            case 1:
                                //下载
                                if(fileOrDirList.get(parentPosition)){
                                    //文件
                                    //判断读写权限是否正常
                                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                                        String[]  needPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                        List<String> losePermissions = new ArrayList<>();
                                        for(int i = 0;i < needPermissions.length;i++){
                                            if(getActivity().checkSelfPermission(needPermissions[i]) != PackageManager.PERMISSION_GRANTED){
                                                losePermissions.add(needPermissions[i]);
                                            }
                                        }
                                        if(!losePermissions.isEmpty()){
                                            //权限不正常
                                            String[] deneyPermissions = losePermissions.toArray(new String[losePermissions.size()]);
                                            requestPermissions(deneyPermissions,REQUEST_PERMISSION_CODE);
                                        }else{
                                            //权限都正常
                                            //检测本地Download目录是否已存在该文件
                                            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                                File checkExists = new File(file.getPath() + "/com.example.wgjrouter/" + fileTitleList.get(parentPosition));

                                                if(!checkExists.exists()){
                                                    eventBus.postSticky(new MessageEvent(1,fileTypeList.get(parentPosition),fileTitleList.get(parentPosition),1,canoniUrl,canoniUlrList.get(parentPosition)));
                                                    Toast.makeText(getActivity(),"已添加到下载队列",Toast.LENGTH_SHORT).show();
                                                }else{
                                                    if(transCoverFile){
                                                        //覆盖下载
                                                        eventBus.postSticky(new MessageEvent(1,fileTypeList.get(parentPosition),fileTitleList.get(parentPosition),1,canoniUrl,canoniUlrList.get(parentPosition)));
                                                        Toast.makeText(getActivity(),"已添加到下载队列（覆盖）",Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(getActivity(),"本地已存在该文件",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }
                                    }else{
                                        //低版本，不需要敏感权限确认
                                        //检测本地Download目录是否已存在该文件
                                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            File checkExists = new File(file.getPath() + "/com.example.wgjrouter/" + fileTitleList.get(parentPosition));

                                            if(!checkExists.exists()){
                                                eventBus.postSticky(new MessageEvent(1,fileTypeList.get(parentPosition),fileTitleList.get(parentPosition),1,canoniUrl,canoniUlrList.get(parentPosition)));
                                                Toast.makeText(getActivity(),"已添加到下载队列",Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getActivity(),"本地已存在该文件",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }else{
                                    Toast.makeText(getActivity(),"抱歉，暂不支持下载文件夹",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 2:
                                //分析
                                if(fileOrDirList.get(parentPosition)){
                                    //文件
                                    AlertDialog adf = null;
                                    AlertDialog.Builder adbf = new AlertDialog.Builder(getActivity());
                                    adbf.setTitle(fileTitleList.get(parentPosition));
                                    adbf.setMessage(fileInfosList.get(parentPosition));
                                    adbf.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    adf = adbf.create();
                                    adf.show();
                                }else{
                                    //分析文件夹内含子项，耗时操作
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            List<Integer> listSize = new ArrayList<>();
                                            listSize = SambaUtils.getInstance(waitChangeTitle).countDirSon(canoniUrl + fileTitleList.get(parentPosition));
                                            if(listSize != null){
                                                Message msg = new Message();
                                                msg.what = 128;
                                                msg.obj = listSize;
                                                msg.arg1 = parentPosition;
                                                hanlderFile.sendMessage(msg);
                                            }else{
                                                //分析子项数出现异常
                                                Message msg = new Message();
                                                msg.what = 127;
                                                msg.obj = "抱歉，分析当前文件夹失败";
                                                hanlderFile.sendMessage(msg);
                                            }
                                        }
                                    }).start();
                                }
                                break;
                            case 3:
                                //重命名
                                final AlertDialog adr = new AlertDialog.Builder(getActivity()).create();
                                View viewR = View.inflate(getActivity(),R.layout.dialog_with_edittext,null);
                                TextView title = viewR.findViewById(R.id.dialog_with_et_title_tv);
                                final EditText newname = viewR.findViewById(R.id.dialog_with_et_input_et);
                                Button btnok = viewR.findViewById(R.id.dialog_with_et_ok_btn);
                                Button btnno = viewR.findViewById(R.id.dialog_with_et_no_btn);

                                title.setText("请输入新文件名");
                                newname.setHint("文件记得加后缀");
                                //显示时去掉文件夹的后缀符
                                newname.setText(fileTitleList.get(parentPosition).replace("/",""));

                                adr.setView(viewR);
                                adr.show();

                                btnok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        adr.dismiss();

                                        newfname = newname.getText().toString();

                                        if(newfname.contains("\\") || newfname.contains("/") | newfname.contains(":") || newfname.contains("*") || newfname.contains("?") || newfname.contains("<") || newfname.contains(">") || newfname.contains("|")){
                                            Toast.makeText(getActivity(),"名称不可包含:\\/:*?<>|",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        //新名称格式判断
                                        if(!fileOrDirList.get(parentPosition)){
                                            //文件夹
                                            newfname = newfname + "/";
                                        }

                                        if(newname.equals("")){
                                            Toast.makeText(getActivity(),"新的名称不可为空",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if(newfname.equals(fileTitleList.get(parentPosition))){
                                            Toast.makeText(getActivity(),"名称未做改变",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if(SambaUtils.getInstance(waitChangeTitle).renameFile(canoniUrl + fileTitleList.get(parentPosition),canoniUrl + newfname)){
                                                    Message msg = new Message();
                                                    msg.what = 130;
                                                    hanlderFile.sendMessage(msg);
                                                }else{
                                                    Message msg = new Message();
                                                    msg.what = 129;
                                                    msg.obj = "抱歉，重命名失败，权限不足";
                                                    hanlderFile.sendMessage(msg);
                                                }
                                            }
                                        }).start();
                                    }
                                });
                                btnno.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        adr.dismiss();
                                    }
                                });
                                break;
                        }
                    }
                });

                ColorDrawable cd = new ColorDrawable(0xb0000000);
                //new PaintDrawable(Color.parseColor("#66000000"))
                popupWindow.setBackgroundDrawable(cd);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);

                int mRealSizeWidth;
                int mRealSizeHeight;

                WindowManager windowManager = (WindowManager) getActivity().getApplication().getSystemService(Context.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                Point outPoint = new Point();
                if(Build.VERSION.SDK_INT >= 19){
                    //考虑到虚拟按键
                    display.getRealSize(outPoint);
                }else{
                    display.getSize(outPoint);
                }

                mRealSizeWidth = outPoint.x;
                mRealSizeHeight = outPoint.y;

                popupWindow.showAtLocation(parentView,Gravity.CENTER,0,0);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(myFgLvArrayAdapter.canFresh()){
                    if(recyclerView.canScrollVertically(1)){
                        fg_file_sr.setEnabled(true);
                    }
                }else{
                    fg_file_sr.setEnabled(false);
                }
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.file_option_download:
                        //批量下载
                        List<Integer> selectIndex = myFgLvArrayAdapter.getSelectItemIndex();
                        final ArrayList<String> selectItemTit = new ArrayList<>();
                        final ArrayList<String> selectItemUrl = new ArrayList<>();

                        for(int i = 0;i < selectIndex.size();i++){
                            int position = selectIndex.get(i);
                            if(fileOrDirList.get(position)){
                                //仅文件添加到下载队列中
                                selectItemTit.add(fileTitleList.get(position));
                                selectItemUrl.add(canoniUlrList.get(position));
                            }
                        }

                        if(selectIndex.size() > 0){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                                    for(int i = 0;i < selectItemTit.size();i++){
                                        boolean isDownloaded = new File(file.getPath() + "/com.example.wgjrouter/" + selectItemTit.get(i)).exists();//本地是否存在已完成的下载文件

                                        if(!isDownloaded){
                                            //本地不存在下载文件
                                            EventBus.getDefault().postSticky(new MessageEvent(1,MyFileUtils.getInstance().checkFileType(selectItemUrl.get(i)),selectItemTit.get(i),1,"",selectItemUrl.get(i)));
                                        }else if(transCoverFile){
                                            //本地已存在完整下载文件，覆盖下载
                                            EventBus.getDefault().postSticky(new MessageEvent(1,MyFileUtils.getInstance().checkFileType(selectItemUrl.get(i)),selectItemTit.get(i),1,"",selectItemUrl.get(i)));
                                        }
                                    }
                                }
                            }).start();

                            //取消掉勾选项
                            myFgLvArrayAdapter.selectNone();
                            Toast.makeText(getActivity(),"添加到下载队列（仅文件）",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(),"无选择项",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.file_option_delete:
                        List<Integer> deleteIndex = myFgLvArrayAdapter.getSelectItemIndex();
                        final List<String> deleteItemUrl = new ArrayList<>();
                        List<String> listTit = new ArrayList<>();

                        for(int i = 0;i < deleteIndex.size();i++){
                            int position = deleteIndex.get(i);
                            deleteItemUrl.add(canoniUlrList.get(position));

                            listTit.add(fileTitleList.get(position));
                        }

                        final int deleteCounts = listTit.size();
                        if(deleteCounts > 0){
                            String[] arrayTit = listTit.toArray(new String[listTit.size()]);

                            AlertDialog ad;
                            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                            adb.setTitle("删除 " + deleteCounts + " 项，" + "请确认");
                            adb.setItems(arrayTit,null);
                            adb.setPositiveButton("删除(不可恢复)", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    View view = View.inflate(getActivity(),R.layout.dialog_waitting_doing,null);

                                    alertDialog = new AlertDialog.Builder(getActivity())
                                            .setView(view)
                                            .create();
                                    alertDialog.show();

                                    //退出多选模式
                                    isEdit = false;
                                    setStateEdit(isEdit);

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int delCount = 0;

                                            for(int i = 0;i < deleteCounts;i++){
                                                //每一项挨个删除
                                                SambaUtils.getInstance(waitChangeTitle).deleteItem(deleteItemUrl.get(i),"");

                                                delCount++;

                                                Message msg = new Message();
                                                msg.obj = "删除进度：" +  delCount + "/" + deleteCounts;
                                                msg.what = 132;
                                                msg.arg1 = 0;
                                                hanlderFile.sendMessage(msg);
                                            }

                                            Message msg = new Message();
                                            msg.what = 132;
                                            msg.arg1 = 1;
                                            hanlderFile.sendMessage(msg);
                                        }
                                    }).start();
                                }
                            });
                            adb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            ad = adb.create();
                            ad.show();
                            ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                            ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                        }else{
                            Toast.makeText(getActivity(),"无选择项",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.file_option_selectall:
                        if(myFgLvArrayAdapter.isSelectAll()){
                            myFgLvArrayAdapter.selectNone();
                        }else{
                            myFgLvArrayAdapter.selectAll();
                        }
                        break;
                    case R.id.file_option_operate:
                        if(canoniUrl.equals(rootUrl)){
                            Toast.makeText(getActivity(),"抱歉,根目录不允许此操作",Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        //多选操作
                        if(myFgLvArrayAdapter.getMultiChoiceMode() == 0){
                            isEdit = true;
                            setStateEdit(isEdit);
                        }
                        break;
                    case R.id.file_option_upload:
                        //上传文件
                        if(canoniUrl.equals(rootUrl)){
                            Toast.makeText(getActivity(),"抱歉,根目录不允许此操作",Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        //判断读写权限是否正常
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                            String[]  needPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            List<String> losePermissions = new ArrayList<>();
                            for(int i = 0;i < needPermissions.length;i++){
                                if(getActivity().checkSelfPermission(needPermissions[i]) != PackageManager.PERMISSION_GRANTED){
                                    losePermissions.add(needPermissions[i]);
                                }
                            }
                            if(!losePermissions.isEmpty()){
                                //权限不正常
                                String[] deneyPermissions = losePermissions.toArray(new String[losePermissions.size()]);
                                requestPermissions(deneyPermissions,REQUEST_PERMISSION_CODE);
                            }else{
                                //权限都正常
                                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                    //当前目录上传文件
                                    Intent selFile = new Intent(Intent.ACTION_GET_CONTENT);
                                    selFile.setType("*/*");
                                    selFile.addCategory(Intent.CATEGORY_OPENABLE);
                                    try {
                                        startActivityForResult(Intent.createChooser(selFile,"Select a File to Upload"),FILE_REQUEST_CODE);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(getActivity(),"抱歉，未找到文件管理器",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }else{
                            //低版本，不需要敏感权限确认
                            //检测本地Download目录是否已存在该文件
                            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                //当前目录上传文件
                                Intent selFile = new Intent(Intent.ACTION_GET_CONTENT);
                                selFile.setType("*/*");
                                selFile.addCategory(Intent.CATEGORY_OPENABLE);
                                try {
                                    startActivityForResult(Intent.createChooser(selFile,"Select a File to Upload"),FILE_REQUEST_CODE);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getActivity(),"抱歉，未找到文件管理器",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        break;
                    case R.id.file_option_newfolder:
                        /**
                         * 当前目录新建文件夹
                         * 1.根目录不能用于新建、上传、删除等操作
                         */
                        if(canoniUrl.equals(rootUrl)){
                            Toast.makeText(getActivity(),"抱歉,根目录不允许此操作",Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();

                        View viewDialog = View.inflate(getActivity(),R.layout.dialog_with_edittext,null);

                        TextView title = viewDialog.findViewById(R.id.dialog_with_et_title_tv);
                        final EditText folder = viewDialog.findViewById(R.id.dialog_with_et_input_et);
                        Button btnok = viewDialog.findViewById(R.id.dialog_with_et_ok_btn);
                        Button btnno = viewDialog.findViewById(R.id.dialog_with_et_no_btn);

                        title.setText("请输入新文件夹名");

                        dialog.setView(viewDialog);
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                        btnok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                newFolderName = folder.getText().toString();

                                if(!newFolderName.equals("")){
                                    dialog.dismiss();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(SambaUtils.getInstance(waitChangeTitle).newFolder(canoniUrl,newFolderName)){

                                                jsonFileDir = SambaUtils.getInstance(waitChangeTitle).getPathDirFile(canoniUrl);

                                                if(!jsonFileDir.startsWith("Error:")){
                                                    handler.post(okrun);
                                                }else{
                                                    newFolderName = null;
                                                    handler.post(errun);
                                                }

                                                isFresh = false;
                                            }else{
                                                newFolderName = null;
                                                Toast.makeText(getActivity(),"抱歉，新建文件夹失败",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).start();
                                }else{
                                    Toast.makeText(getActivity(),"请输入正确的文件夹名称",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        btnno.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        break;
                    case R.id.file_option_count:
                        AlertDialog.Builder adbc = new AlertDialog.Builder(getActivity());
                        adbc.setTitle("当前目录统计：" );
                        adbc.setMessage("文件夹：" + String.valueOf(dirCounts) +
                                "\n文件：" + String.valueOf(fileCounts));
                        adbc.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                        break;
                    case R.id.file_option_backroot:
                        parentTitBack.clear();
                        parentUrlBack.clear();

                        toolbarTile = "";
                        toolbar.setTitle(routerName);
                        toolbar.setNavigationIcon(null);
                        canoniUrl = rootUrl;

                        fresh(canoniUrl);
                        break;
                    case R.id.file_option_change_router:
                        if(wifi_default_config){
                            //切到智能路由器
                            gateway_config = sharedPreferences.getString("gateway_config","");
                            samba_config = sharedPreferences.getString("samba_config","");
                            link_diskdir_config = sharedPreferences.getString("link_diskdir_config","");

                            if(gateway_config != "" && samba_config != "" && link_diskdir_config != ""){
                                routerType = 1;
                                routerHost = "http://" + gateway_config + "/" + link_diskdir_config + "/";
                                rootUrl = "smb://" + samba_config + "@" + gateway_config + "/";
                                canoniUrl = "smb://" + samba_config + "@" + gateway_config + "/";

                                wifi_default_config = false;
                                toolbarTile = "";
                                routerName = "智能路由器";
                                waitChangeTitle = true;

                                myFgLvArrayAdapter.initRouterParams(routerType,routerHost);
                                fresh(canoniUrl);
                            }else{
                                Toast.makeText(getActivity(),"智能路由器未初始化配置",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            //切到默认路由器Holl
                            routerType = 0;
                            routerHost = Consts.NET_HOST_HOLL;
                            rootUrl = Consts.ROOT_URL_HOLL;
                            canoniUrl = Consts.CANONI_URL_HOLL;

                            wifi_default_config = true;
                            toolbarTile = "";
                            routerName = "Holl微路由";
                            waitChangeTitle = true;

                            myFgLvArrayAdapter.initRouterParams(routerType,routerHost);
                            fresh(canoniUrl);
                        }
                        break;
                }
                return true;
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myFgLvArrayAdapter.getMultiChoiceMode() == 0){
                    backFatherDir();
                }else{
                    isEdit = false;
                    setStateEdit(isEdit);
                }
            }
        });

        //当前目录搜索
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutSearch.setVisibility(View.GONE);

                if(!editTextSearch.getText().toString().equals("")){
                    searchFile(editTextSearch.getText().toString());
                }
            }
        });

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    linearLayoutSearch.setVisibility(View.GONE);

                    //搜索
                    if(!editTextSearch.getText().toString().equals("")){
                        searchFile(editTextSearch.getText().toString());
                    }
                }
                return true;
            }
        });

        fg_file_sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fresh(canoniUrl);

                fg_file_sr.setRefreshing(false);
            }
        });

        if(toolbarTile.equals("")){
            toolbar.setTitle(routerName);
            toolbar.setNavigationIcon(null);
        }else{
            toolbar.setTitle(toolbarTile);
            toolbar.setNavigationIcon(R.mipmap.back);

            if(isEdit){
                isEdit = false;
                setStateEdit(false);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getEventType()){
            case 3:
                //上传成功，刷新(切换到其它目录时不受影响，仅是刷新当前路径)
                fresh(canoniUrl);
                break;
            case 4:
                Snackbar.make(fg_file_opendir_cl,"已成功下载到本地",Snackbar.LENGTH_LONG)
                        .setAction("查看", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent look = new Intent(getActivity(),FileTransFinish.class);
                                startActivity(look);
                            }
                        })
                        .setActionTextColor(Color.LTGRAY)
                        .show();
                break;
            case 5:
                //返回上册目录
                backFatherDir();
            case 7:
                //隐藏大图查看器
                if(popupWindowPic != null && popupWindowPic.isShowing()){
                    popupWindowPic.dismiss();
                }
                break;
            case 8:
                if(isEdit){
                    //关闭多选模式
                    isEdit = false;
                    setStateEdit(isEdit);
                }
                break;
            case 22:
                if(event.getFileType() == fileTitleList.size()){
                    //当前全选
                    toolbar.getMenu().findItem(R.id.file_option_selectall).setTitle("全不选");
                }else{
                    toolbar.getMenu().findItem(R.id.file_option_selectall).setTitle("全选");
                }

                toolbar.setTitle("项:" + event.getFileType() + "/" + fileTitleList.size());
                break;
        }
    };

    private void backFatherDir(){
        if(parentUrlBack.size() == 0){
            //没有可返回的父路径
            toolbarTile = "";
            toolbar.setTitle(routerName);
            toolbar.setNavigationIcon(null);
            return;
        }

        if(isFresh){
            return;
        }

        canoniUrl = parentUrlBack.get(parentUrlBack.size() - 1);

        parentUrlBack.remove(parentUrlBack.size() - 1);
        parentTitBack.remove(parentTitBack.size() - 1);

        if(parentTitBack.size() != 0){
            toolbarTile = parentTitBack.get(parentTitBack.size() - 1);
            toolbar.setTitle(toolbarTile);
            toolbar.setNavigationIcon(R.mipmap.back);
        }else{
            toolbarTile = "";
            toolbar.setTitle(routerName);
            toolbar.setNavigationIcon(null);
        }

        fresh(canoniUrl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> deneyPermissionsList = new ArrayList<>();
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults.length > 0){
                for(int i = 0;i < grantResults.length;i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        //权限未放行
                        deneyPermissionsList.add(permissions[i]);
                    }
                }

                if(!deneyPermissionsList.isEmpty()){
                    String[] deneyPermissions = deneyPermissionsList.toArray(new String[deneyPermissionsList.size()]);
                    //判断是否点击了不再提示(是否可再重新申请)
                    boolean ii = shouldShowRequestPermissionRationale(deneyPermissions[0]);

                    //未点击>不再询问，拒绝时返回 true,表名还可再请求权限
                    if(ii){
                        //还可再申请权限
                        new AlertDialog.Builder(getActivity())
                                .setTitle("允许\"微管家\"获取读写存储器权限吗?")
                                .setMessage("微管家在上传、下载文件时需要读写存储器权限")
                                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(permissions,REQUEST_PERMISSION_CODE);
                                    }
                                })
                                .setNegativeButton("不允许", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getActivity(),"用上传、下载功能将受限",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .create().show();
                    }else{
                        Toast.makeText(getActivity(),"未能获得读写存储器权限",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    Runnable okrun = new Runnable() {
        @Override
        public void run() {
            if(waitChangeTitle){
                toolbar.setTitle(routerName);
                toolbar.setNavigationIcon(null);
            }
            waitChangeTitle = false;

            recyclerView.setVisibility(View.VISIBLE);
            fg_file_error_iv.setVisibility(View.GONE);

            try {
                JsonParser jsonParser = new JsonParser();
                JsonArray jsonArray = jsonParser.parse(jsonFileDir).getAsJsonArray();

                //刷新前清空
                fileInfosList.clear();
                myFgLvInfosList.clear();
                fileOrDirList.clear();
                fileTitleList.clear();
                fileTypeList.clear();
                parentUrlList.clear();
                canoniUlrList.clear();
                picItemUrlList.clear();
                videoItemUrlList.clear();
                videoTitleList.clear();
                dirCounts = 0;
                fileCounts = 0;

                int index = 0;
                //文件属性
                String fileInfo = "文件大小：**\n" + "修改日期：##\n";
                for(JsonElement je : jsonArray){
                    SmbFileInfos smbFileInfos = gson.fromJson(je,SmbFileInfos.class);

                    MyRecFileInfos myFgLvInfos = new MyRecFileInfos();
                    myFgLvInfos.setFilePath(smbFileInfos.getCanoniUrl());
                    myFgLvInfos.setFileTitle(smbFileInfos.getName());
                    myFgLvInfos.setFileSize(smbFileInfos.getSize());
                    myFgLvInfos.setSizeUnit(smbFileInfos.getSizeUnit());
                    myFgLvInfos.setFileDate(smbFileInfos.getDate());
                    myFgLvInfos.setIsFile(smbFileInfos.getIsFile());

                    fileInfo = fileInfo.replace("**",String.valueOf(smbFileInfos.getSize()) + smbFileInfos.getSizeUnit());
                    fileInfo = fileInfo.replace("##", MyDateUtils.getInstance().getCurrentDate(smbFileInfos.getDate()));

                    if(smbFileInfos.getIsDir()){
                        myFgLvInfos.setItemCount(smbFileInfos.getCounts());
                    }

                    if(smbFileInfos.getIsFile()){
                        ////Samba链接转真实链接
                        String httpSrcUrl = "";
                        switch (routerType){
                            case 0:
                                httpSrcUrl = smbFileInfos.getCanoniUrl().substring((smbFileInfos.getCanoniUrl().indexOf("/holl/") + 5),smbFileInfos.getCanoniUrl().length());
                                break;
                            case 1:
                                httpSrcUrl = smbFileInfos.getCanoniUrl().substring((smbFileInfos.getCanoniUrl().indexOf("@192.168.80.1/") + 14),smbFileInfos.getCanoniUrl().length());
                                break;
                        }

                        httpSrcUrl = routerHost + httpSrcUrl;

                        String fileUrl  = smbFileInfos.getCanoniUrl().toLowerCase();
                        if(fileUrl.endsWith("png") || fileUrl.endsWith("jpg") || fileUrl.endsWith("bmp") || fileUrl.endsWith("gif") || fileUrl.endsWith("jpeg") || fileUrl.endsWith("ico")){
                            //将当前文件夹所有的图片地址存入List
                            picItemUrlList.add(httpSrcUrl);
                        }
                        if(fileUrl.endsWith("avi") || fileUrl.endsWith("mov") || fileUrl.endsWith("mp4") || fileUrl.endsWith("mkv") || fileUrl.endsWith("wmv") || fileUrl.endsWith("flv") || fileUrl.endsWith("rmvb")){
                            //将当前文件夹所有的视频地址存入list
                            videoItemUrlList.add(httpSrcUrl);
                            videoTitleList.add(fileUrl.substring(fileUrl.lastIndexOf("/") + 1,fileUrl.lastIndexOf(".")));
                        }
                    }

                    if(smbFileInfos.getName().equals(newFolderName + "/")){
                        //该项为刚新建的文件夹
                        newFolderIndex = index;
                    }

                    fileInfosList.add(fileInfo);
                    fileOrDirList.add(smbFileInfos.getIsFile());
                    fileTitleList.add(smbFileInfos.getName());
                    fileTypeList.add(CheckFileType(smbFileInfos.getCanoniUrl()));
                    parentUrlList.add(smbFileInfos.getParentUrl());
                    canoniUlrList.add(smbFileInfos.getCanoniUrl());

                    //初始化文件夹、文件的数量，用于文件、文件夹数量统计
                    if(smbFileInfos.getIsDir()){
                        dirCounts++;
                    }else{
                        fileCounts++;
                    }

                    myFgLvInfosList.add(myFgLvInfos);

                    index = index + 1;
                }

                myFgLvArrayAdapter.setCanInitData(true);
                myFgLvArrayAdapter.notifyDataSetChanged();

                //新建文件夹时跳到该新文件夹的索引
                if(newFolderName != null){
                    if(newFolderIndex != 0){
                        //匹配到新建的文件夹名
                        recyclerView.scrollToPosition(newFolderIndex);
                        newFolderName = "";
                        newFolderIndex = 0;
                    }
                }else if(longClickItemIndex == 0){
                    recyclerView.scrollToPosition(0);
                    longClickItemIndex = 0;
                }

                fg_file_pb.setVisibility(View.GONE);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                fg_file_pb.setVisibility(View.GONE);
            }
        }
    };

    Runnable errun = new Runnable() {
        @Override
        public void run() {
            recyclerView.setVisibility(View.GONE);
            fg_file_error_iv.setVisibility(View.VISIBLE);

            if(waitChangeTitle == true){
                if(wifi_default_config == false){
                    //从Holl切智能失败
                    routerType = 0;
                    routerHost = Consts.NET_HOST_HOLL;
                    rootUrl = Consts.ROOT_URL_HOLL;
                    canoniUrl = Consts.CANONI_URL_HOLL;
                    routerName = "Holl微路由";

                    toolbar.setTitle(routerName);
                    toolbar.setNavigationIcon(null);

                    wifi_default_config = true;
                }else{
                    routerType = 1;
                    routerHost = "http://" + gateway_config + "/" + link_diskdir_config + "/";
                    rootUrl = "smb://" + samba_config + "@" + gateway_config + "/";
                    canoniUrl = "smb://" + samba_config + "@" + gateway_config + "/";
                    routerName = "智能路由器";

                    toolbar.setTitle(routerName);
                    toolbar.setNavigationIcon(null);

                    wifi_default_config = false;
                }

                Toast.makeText(getActivity(),"切换失败，回滚中...",Toast.LENGTH_SHORT).show();
                myFgLvArrayAdapter.initRouterParams(routerType,routerHost);
                fresh(canoniUrl);
            }

            waitChangeTitle = false;
            fg_file_pb.setVisibility(View.GONE);
        }
    };

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if(isEdit){
            getActivity().getMenuInflater().inflate(R.menu.file_edit_menu,menu);
            toolbar.setTitle("项:" + myFgLvArrayAdapter.getSelectItemIndex().size() + "/" + fileTitleList.size());
            toolbar.setNavigationIcon(R.mipmap.rollback);
        }else{
            getActivity().getMenuInflater().inflate(R.menu.file_option_menu,menu);

            if(toolbarTile.equals("")){
                toolbar.setTitle(routerName);
                toolbar.setNavigationIcon(null);
            }else{
                toolbar.setTitle(toolbarTile);
                toolbar.setNavigationIcon(R.mipmap.back);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_option_menu,menu);
    }

    //模糊搜索当前界面
    private void searchFile(String fileName){
        if(fileTitleList.size() == 0){
            return;
        }

        for(int i = 0;i < fileTitleList.size();i++){
            if(fileTitleList.get(i).indexOf(fileName) != -1){
                //匹配
            }
        }

        toolbar.setTitle("搜索:" + fileName);
        toolbar.setNavigationIcon(R.mipmap.rollback);
    }

    //全局搜索
    private void searchAllFile(){

    }

    //查看文件
    private void showFile(int fileType,String srcNetUrl){
        switch(fileType){
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
                fg_file_showpic_label.setTag("show");
                //根据ListView的点击项获取图片List中的对应索引
                clickPicIndex = picItemUrlList.indexOf(srcNetUrl);

                if(parentView != null){
                    View viewPoP = LayoutInflater.from(getActivity()).inflate(R.layout.layout_fg_file_pop_bigpic,null);

                    popupWindowPic = new PopupWindow(viewPoP,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

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
                                Toast.makeText(getActivity(),"这是第一张呀",Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getActivity(),"最后一张了",Toast.LENGTH_SHORT).show();
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

                    popupWindowPic.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            fg_file_showpic_label.setTag("hiddle");
                        }
                    });

//                    bigPicIV.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View view, MotionEvent motionEvent) {
//                            switch (motionEvent.getAction()){
//                                case MotionEvent.ACTION_DOWN:
//                                    mPosX = motionEvent.getX();
//                                    mPosY = motionEvent.getY();
//                                    break;
//                                case MotionEvent.ACTION_MOVE:
//                                    break;
//                                case MotionEvent.ACTION_UP:
//                                    mCurPosX = motionEvent.getX();
//                                    mCurPosY = motionEvent.getY();
//
//                                    if(mCurPosX - mPosX > 50 && Math.abs(mCurPosY - mPosY) < 30){
//                                        //右滑动,上一章图片
//                                        if(!isShowPic){
//                                            isShowPic = true;
//
//                                            clickPicIndex = clickPicIndex - 1;
//
//                                            if(clickPicIndex >= 0){
//                                                Picasso.get().load(picItemUrlList.get(clickPicIndex))
//                                                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
//                                                        .placeholder(R.mipmap.loading_zhanwei)
//                                                        .error(R.mipmap.loading_faild)
//                                                        .into(bigPicIV);
//                                            }else{
//                                                clickPicIndex = 0;
//                                                Toast.makeText(getActivity(),"这是第一张呀",Toast.LENGTH_SHORT).show();
//                                            }
//                                            isShowPic = false;
//                                        }
//                                    }else if(mCurPosX - mPosX < -50 && Math.abs(mCurPosY - mPosY) < 30){
//                                        //左滑动，下一站图片
//                                        if(!isShowPic){
//                                            isShowPic = true;
//
//                                            clickPicIndex = clickPicIndex + 1;
//
//                                            if(clickPicIndex < picItemUrlList.size()){
//                                                Picasso.get().load(picItemUrlList.get(clickPicIndex))
//                                                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
//                                                        .placeholder(R.mipmap.loading_zhanwei)
//                                                        .error(R.mipmap.loading_faild)
//                                                        .into(bigPicIV);
//                                            }else{
//                                                clickPicIndex = clickPicIndex - 1;
//                                                Toast.makeText(getActivity(),"最后一张了",Toast.LENGTH_SHORT).show();
//                                            }
//                                            isShowPic = false;
//                                        }
//                                    }else if(mCurPosY - mPosY < -50 && Math.abs(mCurPosX - mPosX) < 30){
//                                        //上滑动
//
//                                    }else if(mCurPosY - mPosY > 50 && Math.abs(mCurPosX - mPosX) < 30){
//                                        //下滑动，关闭图片
//                                        popupWindow.dismiss();
//                                    }
//                                    break;
//                                default:
//                                    break;
//                            }
//                            return true;
//                        }
//                    });

                    Picasso.get().load(srcNetUrl)
                            .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                            .placeholder(R.mipmap.loading_zhanwei)
                            .error(R.mipmap.loading_faild)
                            .into(bigPicIV);

                    popupWindowPic.showAtLocation(parentView, Gravity.CENTER,0,0);
                }
                break;
            case 15:
                //视频
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean default_resolver_inner = sp.getBoolean("default_resolver_inner",true);

                if(default_resolver_inner){
                    //内部解析
                    Intent intent = new Intent(getActivity(),VideoPlayVTM.class);
                    intent.putExtra("videoType",0);
                    intent.putExtra("videoUrl",srcNetUrl);
                    intent.putExtra("videoTitle",srcNetUrl.substring((srcNetUrl.lastIndexOf("/") + 1),srcNetUrl.length()));
                    intent.putStringArrayListExtra("videoUrlList",videoItemUrlList);
                    intent.putStringArrayListExtra("videoTitleList",videoTitleList);
                    startActivity(intent);
                }else{
                    MyPhoneUtils.getInstance(getActivity()).usePlayerPlayNetVideo(srcNetUrl);
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

        isFresh = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == FILE_REQUEST_CODE){

            if(data == null){
                return;
            }

            Uri uri = data.getData();

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
                //4.4以上系统
                urlPath = getPathFromUri(getContext(),uri);
            }else{
                urlPath = getRealPathFromUri(getContext(),uri);
            }

            if(urlPath == null){
                //文件真实路径解析失败
                Toast.makeText(getActivity(),"抱歉，文件路径解析失败",Toast.LENGTH_SHORT).show();
                return;
            }

            final File file = new File(urlPath);
            fileType = CheckFileType(urlPath);

            //检测文件是否存在
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(SambaUtils.getInstance(waitChangeTitle).checkFileOrDirExist(canoniUrl + file.getName())){
                        Message msg = new Message();
                        msg.what = 126;
                        msg.obj = file.getName();
                        msg.arg1 = fileType;
                        hanlderFile.sendMessage(msg);
                    }else{
                        eventBus.postSticky(new MessageEvent(2,fileType,file.getName(),0,canoniUrl,urlPath));
                    }
                }
            }).start();
        }
    }

    /**
     * 4.4以上系统
     * @param uri
     * @return
     */
    private String getPathFromUri(Context context,Uri uri){
        Log.d("zouguo","1:" + uri.getScheme());
        Log.d("zouguo","2:" + uri.getAuthority());
        Log.d("zouguo","2.1:" + uri.getSchemeSpecificPart());
        Log.d("zouguo","2.2:" + uri.getEncodedAuthority());
        Log.d("zouguo","2.3:" + uri.getEncodedSchemeSpecificPart());

        if(DocumentsContract.isDocumentUri(context,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            Log.d("zouguo","3:" + docId);

            if("com.android.externalstorage.documents".equals(uri.getAuthority())){
                String[] split = docId.split(":");
                String type = split[0];

                if("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else{
                    return "/storage/".concat(type).concat("/").concat(split[1]);
                }
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                if(docId.startsWith("raw")){
                    Log.d("zouguo","3.1:" + docId);

                    String[] split = docId.split(":");
                    String type = split[0];
                    return split[1];
                }

                Log.d("zouguo","3.2:" + docId);
                //7.0
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                    return getFilePath(context,contentUri,null,null);
                }else{
                    //访问all_downloads需要具备android.permission.ACCESS_ALL_DOWNLOADS权限,系统级别的应用才可以访问
//                    DownloadManager dm = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//
//                    DownloadManager.Query dmq = new DownloadManager.Query();//.setFilterById(Long.parseLong(docId))
//                    Cursor c = dm.query(dmq);
//
//                    if(c != null && c.moveToFirst()){
//                        int idx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
//                        String name = c.getString(idx);
//
//                        File file = new File(name);
//
//                        Log.d("zouguo","2--8.0往上系统文件路径获取" + name);
//                        Log.d("zouguo","2--8.0往上系统文件路径获取" + file.getPath());
//                        Log.d("zouguo","2--8.0往上系统文件路径获取" + file.getAbsolutePath());
//
//                        c.close();
//                    }else{
//                        Log.d("zouguo","2--8.0失败");
//                    }

                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/all_downloads"),Long.valueOf(docId));
                    Log.d("zouguo","8.0往上系统文件路径获取");
                    Log.d("zouguo","8.0：" + contentUri.getPath());
                    return getFilePath(context,contentUri,null,null);
                }
            }else if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if("image".equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }else if("video".equals(type)){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }else if("audio".equals(type)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String [] selectionArgs = new String[]{split[1]};

                return getFilePath(context,contentUri,selection,selectionArgs);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            return getFilePath(context,uri,null,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }
        return null;
    }

    /**
     * 4.4以下系统
     * @param uri
     * @return
     */
    private String getRealPathFromUri(Context context,Uri uri){
        String path = null;

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri,proj,null,null,null);
        if(null != cursor && cursor.moveToFirst()){
            int cindex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(cindex);
            cursor.close();
        }
        return path;
    }

    private String getFilePath(Context context,Uri uri,String selection,String[] selectionArgs){
        Cursor cursor = null;
        String column = "_data";
        String[] proj = {column};

        try {
            cursor = context.getContentResolver().query(uri,proj,selection,selectionArgs,null);
            if(cursor != null && cursor.moveToFirst()){
                int cIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(cIndex);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return null;
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

    private void initNotification(String title,String content){
        NotificationChannel notificationChannel = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setShowBadge(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setVibrationPattern(new long[]{0});
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            nb = new Notification.Builder(getActivity(),CHANNEL_ID);
            nb.setContentTitle(title)
                    .setSmallIcon(R.mipmap.smallicon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setContentText(content)
                    .setProgress(100,0,false)
                    .build();
            notification = nb.build();
        }else{
            ncb = new NotificationCompat.Builder(getActivity());
            ncb.setContentTitle(title)
                    .setSmallIcon(R.mipmap.smallicon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setContentText(content)
                    .setProgress(100,0,false)
                    .setVibrate(new long[]{0})
                    .build();
            notification = ncb.build();
        }

        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(1,notification);
    }

    private void fresh(final String urlPath){
        if(isFresh){
            return;
        }

        isFresh = true;

        recyclerView.setVisibility(View.GONE);
        fg_file_pb.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                jsonFileDir = SambaUtils.getInstance(waitChangeTitle).getPathDirFile(urlPath);

//                Log.d("zouguo","刷新：" + urlPath);
//                Log.d("zouguo","结果：" + jsonFileDir);

                if(!jsonFileDir.startsWith("Error:")){
                    handler.post(okrun);
                }else{
                    handler.post(errun);
                }

                isFresh = false;
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        eventBus.unregister(this);
        getActivity().unregisterReceiver(networkChangeReceive);
    }

    //进入、退出编辑状态
    private void setStateEdit(boolean isEdit){
        if(isEdit){
            getActivity().invalidateOptionsMenu();
            myFgLvArrayAdapter.setMultiChoiceMode(1);
            fg_file_sr.setEnabled(false);
        }else{
            getActivity().invalidateOptionsMenu();
            myFgLvArrayAdapter.setMultiChoiceMode(0);
            fg_file_sr.setEnabled(true);

            MenuItem menuItem = toolbar.getMenu().findItem(R.id.file_option_selectall);
            if(menuItem != null){
                if(myFgLvArrayAdapter.isSelectAll()){
                    menuItem.setTitle("全不选");
                }else{
                    menuItem.setTitle("全选");
                }
            }
        }
    }

    private void setOutsideAlpha(float alpha){
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        params.alpha = alpha;
        getActivity().getWindow().setAttributes(params);
    }
}