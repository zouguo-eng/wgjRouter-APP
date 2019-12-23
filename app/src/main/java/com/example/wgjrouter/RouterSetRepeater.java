package com.example.wgjrouter;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MyBaLvAPInfosAdapter;
import com.example.bean.MyBaLvApInfos;
import com.example.consts.Consts;

import org.apache.commons.net.telnet.TelnetClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RouterSetRepeater extends AppCompatActivity {

    private ProgressBar router_setrepeater_pb;
    private Toolbar router_setrepeater_tb;
    private Switch router_setrepeater_switch;
    private ListView router_setrepeater_lv;
    private ImageView router_setrepeater_iv;

    private List<MyBaLvApInfos> myBaLvAPInfosList;
    private MyBaLvAPInfosAdapter myBaLvAPInfosAdapter;

    private TelnetClient telnetClient = null;
    private InputStream is = null;
    private PrintStream ps = null;
    private char prompt = '$';
    private List<Integer> apindexList = new ArrayList<>();
    private List<String> apssidList = new ArrayList<>();
    private List<Integer> apsignalList = new ArrayList<>();
    private List<String> apencryptList = new ArrayList<>();

    private List<Boolean> apBridgedList = new ArrayList<>();

    private boolean isFresh = false;
    //必须开启了中继后，才能进行关闭中继
    private boolean isRepeater = false;
    private String apNamed = null;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    router_setrepeater_pb.setVisibility(View.GONE);
                    router_setrepeater_tb.setTitle("无线中继");
                }
            });

            switch (msg.what){
                case 0:
                    Toast.makeText(RouterSetRepeater.this,"抱歉，与微路由通信超时",Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    //AP获取完成
                    Log.d("zouguo","扫描结束");
                    apBridgedList.clear();
                    myBaLvAPInfosList.clear();

                    if(apindexList.size() == 0){
                        Toast.makeText(RouterSetRepeater.this,"未能搜到周围AP信息",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for(int i = 0;i < apssidList.size();i++){
                        MyBaLvApInfos myBaLvAPInfos = new MyBaLvApInfos();
                        myBaLvAPInfos.setAPindex(apindexList.get(i));
                        myBaLvAPInfos.setAPssid(apssidList.get(i));
                        myBaLvAPInfos.setAPsignal(apsignalList.get(i));
                        myBaLvAPInfos.setAPencrypt(apencryptList.get(i));

                        if(apNamed != null && apssidList.get(i).equals(apNamed)){
                            //当前桥接的WiFi
                            myBaLvAPInfos.setAPbridged(true);
                            apBridgedList.add(true);
                        }else{
                            myBaLvAPInfos.setAPbridged(false);
                            apBridgedList.add(false);
                        }

                        myBaLvAPInfosList.add(myBaLvAPInfos);
                    }

                    myBaLvAPInfosAdapter.notifyDataSetChanged();
                    break;
                case 7:
                    //XML解析失败
                    Toast.makeText(RouterSetRepeater.this,"抱歉，解析数据时发生异常",Toast.LENGTH_SHORT).show();
                    break;
                case 8:
                    //获取中继状态、扫描AP信息完成
                    break;
                case 9:
                    if(msg.arg1 == 2){
                        return;
                    }

                    Toast.makeText(RouterSetRepeater.this,"无线中继配置完成，请重连热点",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 10:
                    if(msg.arg1 == 1){
                        //关闭中继
                        apNamed = null;
                    }

                    //获取到中继状态
                    router_setrepeater_switch.setEnabled(true);
                    if(msg.obj.toString().contains("sta")){
                        //中继配置文件正常
                        isRepeater = true;
                        router_setrepeater_switch.setChecked(true);
                    }else{
                        isRepeater = false;
                        router_setrepeater_switch.setChecked(false);
                    }
                    break;
                case 11:
                    String[] a = msg.obj.toString().split("\n");

                    if(a.length == 3){
                        apNamed = a[1];

                        try {
                            apNamed = new String(apNamed.trim().getBytes("iso-8859-1"),"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        if(apNamed.contains("?")){
                            apNamed = apNamed.replace("?","");
                        }

                        Log.d("zouguo","apNamed:" + apNamed);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_setrepeater);

        initView();

        //获取中继状态，存在异常的可能性
        checkRepeaterStatus("root","holl0311",Consts.REPEATER_CHECK_STATUS);
    }

    private void initView(){
        router_setrepeater_pb = findViewById(R.id.router_setrepeater_pb);
        router_setrepeater_tb = findViewById(R.id.router_setrepeater_tb);
        router_setrepeater_tb.setNavigationIcon(R.mipmap.back);
        router_setrepeater_tb.setTitle("中继初始化...");

        myBaLvAPInfosList = new ArrayList<>();
        myBaLvAPInfosAdapter = new MyBaLvAPInfosAdapter(RouterSetRepeater.this,myBaLvAPInfosList);
        router_setrepeater_lv = findViewById(R.id.router_setrepeater_lv);
        router_setrepeater_lv.setAdapter(myBaLvAPInfosAdapter);

        router_setrepeater_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //判断加密方式
                if(!apencryptList.get(position).equals("")){
                    if(apBridgedList.get(position)){
                        //当前AP已被中继
                        AlertDialog adf = null;
                        AlertDialog.Builder adbf = new AlertDialog.Builder(RouterSetRepeater.this);
                        adbf.setTitle("当前AP已被中继");
                        adbf.setMessage("是否关闭该中继");
                        adbf.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                router_setrepeater_pb.setVisibility(View.VISIBLE);
                                Toast.makeText(RouterSetRepeater.this,"无线中继配置中，请稍等",Toast.LENGTH_SHORT).show();
                                //关闭中继
                                connectRouter(1,"root","holl0311","","","");
                            }
                        });
                        adbf.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        adf = adbf.create();
                        adf.show();
                        adf.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                    }else{
                        if(apNamed != null){
                            //已经中继了其它AP
                            AlertDialog adf = null;
                            AlertDialog.Builder adbf = new AlertDialog.Builder(RouterSetRepeater.this);
                            adbf.setTitle("已中继其它AP");
                            adbf.setMessage("是否切换中继");
                            adbf.setPositiveButton("切换", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    //关闭中继
                                    connectRouter(2,"root","holl0311","","","");

                                    router_setrepeater_pb.setVisibility(View.VISIBLE);
                                    Toast.makeText(RouterSetRepeater.this,"关闭当前中继，请稍等",Toast.LENGTH_SHORT).show();
                                    try {
                                        //休眠2秒
                                        TimeUnit.SECONDS.sleep(2);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    if(apencryptList.get(position).equals("Other")){
                                        router_setrepeater_pb.setVisibility(View.GONE);
                                        Toast.makeText(RouterSetRepeater.this,"抱歉，无法识别的AP加密模式",Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    //当前热点有加密
                                    final AlertDialog dialogChange = new AlertDialog.Builder(RouterSetRepeater.this).create();

                                    View viewDialog = View.inflate(RouterSetRepeater.this,R.layout.dialog_with_edittext,null);

                                    TextView title = viewDialog.findViewById(R.id.dialog_with_et_title_tv);
                                    final EditText et_appwd = viewDialog.findViewById(R.id.dialog_with_et_input_et);
                                    Button btnok = viewDialog.findViewById(R.id.dialog_with_et_ok_btn);
                                    Button btnno = viewDialog.findViewById(R.id.dialog_with_et_no_btn);

                                    title.setText("请输入:");
                                    et_appwd.setHint( apssidList.get(position) + "的无线密码");

                                    btnok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(et_appwd.getText().toString().length() < 8){
                                                Toast.makeText(RouterSetRepeater.this,"无线密码至少8位字母或数字",Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            if(!et_appwd.getText().toString().equals("")){
                                                dialogChange.dismiss();
                                                router_setrepeater_pb.setVisibility(View.VISIBLE);

                                                Toast.makeText(RouterSetRepeater.this,"无线中继配置中，请稍等",Toast.LENGTH_SHORT).show();
                                                connectRouter(0,"root","holl0311",apssidList.get(position),et_appwd.getText().toString().replaceAll("\\s*",""),apencryptList.get(position));
                                            }else{
                                                Toast.makeText(RouterSetRepeater.this,"抱歉，当前AP需要输入无线密码",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    btnno.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogChange.dismiss();
                                            router_setrepeater_pb.setVisibility(View.GONE);
                                        }
                                    });

                                    dialogChange.setView(viewDialog);
                                    dialogChange.setCancelable(true);
                                    dialogChange.setCanceledOnTouchOutside(false);
                                    dialogChange.show();
                                }
                            });
                            adbf.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            adf = adbf.create();
                            adf.show();
                            adf.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                        }else{
                            if(apencryptList.get(position).equals("Other")){
                                router_setrepeater_pb.setVisibility(View.GONE);
                                Toast.makeText(RouterSetRepeater.this,"抱歉，无法识别的AP加密模式",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //当前热点有加密
                            final AlertDialog dialog = new AlertDialog.Builder(RouterSetRepeater.this).create();

                            View viewDialog = View.inflate(RouterSetRepeater.this,R.layout.dialog_with_edittext,null);

                            TextView title = viewDialog.findViewById(R.id.dialog_with_et_title_tv);
                            final EditText et_appwd = viewDialog.findViewById(R.id.dialog_with_et_input_et);
                            Button btnok = viewDialog.findViewById(R.id.dialog_with_et_ok_btn);
                            Button btnno = viewDialog.findViewById(R.id.dialog_with_et_no_btn);

                            title.setText("请输入:");
                            et_appwd.setHint( apssidList.get(position) + "的无线密码");

                            btnok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(et_appwd.getText().toString().length() < 8){
                                        Toast.makeText(RouterSetRepeater.this,"无线密码至少8位字母或数字",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if(!et_appwd.getText().toString().equals("")){
                                        dialog.dismiss();
                                        router_setrepeater_pb.setVisibility(View.VISIBLE);

                                        Toast.makeText(RouterSetRepeater.this,"无线中继配置中，请稍等",Toast.LENGTH_SHORT).show();
                                        connectRouter(0,"root","holl0311",apssidList.get(position),et_appwd.getText().toString().replaceAll("\\s*",""),apencryptList.get(position));
                                    }else{
                                        Toast.makeText(RouterSetRepeater.this,"抱歉，当前AP需要输入无线密码",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            btnno.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    router_setrepeater_pb.setVisibility(View.GONE);
                                }
                            });

                            dialog.setView(viewDialog);
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    }
                }else{
                    router_setrepeater_pb.setVisibility(View.VISIBLE);
                    //当前热点无加密
                    Toast.makeText(RouterSetRepeater.this,"无线中继配置中，请稍等",Toast.LENGTH_SHORT).show();
                    connectRouter(0,"root","holl0311",apssidList.get(position),"","none");
                }
            }
        });

        router_setrepeater_iv = findViewById(R.id.router_setrepeater_iv);
        router_setrepeater_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFresh){
                    return;
                }
                isFresh = true;

                myBaLvAPInfosList.clear();
                myBaLvAPInfosAdapter.notifyDataSetChanged();
                scanAroundAP();
            }
        });

        router_setrepeater_switch = findViewById(R.id.router_setrepeater_switch);
        router_setrepeater_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    router_setrepeater_iv.setEnabled(true);

                    //打开中继，扫描周围AP
                    scanAroundAP();
                }else{
                    router_setrepeater_iv.setEnabled(false);
                    if(isRepeater){
                        //关闭中继
                        AlertDialog ad = null;
                        AlertDialog.Builder adb = new AlertDialog.Builder(RouterSetRepeater.this);
                        adb.setTitle("关闭提醒");
                        adb.setMessage("是否要关闭无线中继");
                        adb.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(RouterSetRepeater.this,"正在关闭无线中继，请稍等",Toast.LENGTH_SHORT).show();
                                myBaLvAPInfosList.clear();
                                myBaLvAPInfosAdapter.notifyDataSetChanged();

                                connectRouter(1,"root","holl0311","","","");
                            }
                        });
                        adb.setNegativeButton("算了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                router_setrepeater_switch.setChecked(true);
                                dialog.dismiss();
                            }
                        });
                        ad = adb.create();
                        ad.show();
                        ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                        ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                    }else{
                        //中继似乎并没有开启，存在没检测到中继状态的可能性，暂不解决
                    }
                }
            }
        });

        router_setrepeater_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 获取中继状态
     */
    private void getRepeaterState(){
        sendMsgToRouter(3,Consts.ROUTER_GET_INFO);
    }

    /**
     * 扫描周围AP信息
     */
    private void scanAroundAP(){
        router_setrepeater_pb.setVisibility(View.VISIBLE);

        Log.d("zouguo","扫描周围AP中...");
        sendMsgToRouter(4,Consts.ROUTER_SCAN_AP);
        isFresh = false;
    }

    /**
     * 通过命令行的方式，配置路由器
     * @param user
     * @param password
     * @param STASSID
     * @param STAKEY
     */
    private void connectRouter(final int flag,final String user,final String password,final String STASSID,final String STAKEY,final String ENCRTPT){
        //操作路由器
        new Thread(new Runnable() {
            @Override
            public void run() {
                telnetClient = new TelnetClient();
                try {
                    telnetClient.connect("192.168.80.1",23);
                    is = telnetClient.getInputStream();
                    ps = new PrintStream(telnetClient.getOutputStream());
                    prompt = user.equals("root") ? '#' : '$';

                    //登录
                    telnetLogin(telnetClient,user,password);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (flag){
                        case 0:
                            //开启中继
                            //配置Network
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_NETWORK_FIRST);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_NETWORK_TWO);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_NETWORK_THREE);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_NETWORK_OVER);

                            //配置Wireless
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_FIRST);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_TWO);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_THREE);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_FOUR);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_FIVE.replace("**",STASSID));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_SIX.replace("**",ENCRTPT));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_SEVEN.replace("**",STAKEY));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_WIRELESS_OVER);

                            //配置Apconfig
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_FIRST);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_TWO.replace("**",STASSID));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_THREE.replace("**",STAKEY));
                            //sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_FOUR.replace("**","6"));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_FIVE.replace("**",ENCRTPT));
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_OVER);

                            //配置中继开启标志
                            sendCodeToRouter(telnetClient,Consts.REPEATER_ON_STASTATUS);

                            //重启WIFI,使配置生效
                            sendCodeToRouter(telnetClient,Consts.NETWORK_WIFI_RELAOD);

                            isRepeater = true;
                            break;
                        case 1:
                        case 2:
                            //关闭中继
                            sendCodeToRouter(telnetClient, Consts.REPEATER_DEL_NETWORK);
                            sendCodeToRouter(telnetClient, Consts.REPEATER_SET_NETWORK_OVER);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_DEL_WIRELESS);
                            sendCodeToRouter(telnetClient, Consts.REPEATER_SET_WIRELESS_OVER);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_DEL_APCONFIG);
                            sendCodeToRouter(telnetClient,Consts.REPEATER_SET_APCONFIG_OVER);

                            //配置中继关闭标志
                            sendCodeToRouter(telnetClient,Consts.REPEATER_OFF_STASTATUS);

                            //重启WIFI,使配置生效
                            sendCodeToRouter(telnetClient,Consts.NETWORK_WIFI_RELAOD);

                            isRepeater = false;
                            break;
                    }

                    //退出
                    ps.println("exit\r\n");
                    ps.flush();
                    disConnection();

                    Message msg = new Message();
                    msg.what = 9;
                    msg.arg1 = flag;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();

                    isRepeater = false;

                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }


    private void checkRepeaterStatus(final String user,final String password,final String code){
        //操作路由器
        new Thread(new Runnable() {
            @Override
            public void run() {
                telnetClient = new TelnetClient();
                try {
                    telnetClient.connect("192.168.80.1",23);

                    is = telnetClient.getInputStream();
                    ps = new PrintStream(telnetClient.getOutputStream());

                    prompt = user.equals("root") ? '#' : '$';

                    //登录
                    telnetLogin(telnetClient,user,password);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String status = sendCodeToRouter(telnetClient,code);

                    if(status != null && status.contains("sta")){
                        //中继已开启，获取该AP名称
                        String apName = sendCodeToRouter(telnetClient,Consts.REPEATERED_GET_APNAME);

                        Message msg = new Message();
                        msg.what = 11;
                        msg.obj = apName;
                        handler.sendMessage(msg);
                    }

                    //退出
                    ps.println("exit\r\n");
                    ps.flush();
                    disConnection();

                    Message msg = new Message();
                    msg.what = 10;
                    msg.obj = status;
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();

                    isRepeater = false;

                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disConnection();
    }

    //Telnet登录到微路由
    private void telnetLogin(TelnetClient telnetClient,String user,String password){
        read(telnetClient,"Holl login:");
        write(telnetClient,user);
        read(telnetClient,"Password:");
        write(telnetClient,password);
        read(telnetClient,prompt + " ");
        write(telnetClient,"cd /");
        read(telnetClient,prompt + " ");
    }

    private String sendCodeToRouter(TelnetClient telnetClient,String code){
        write(telnetClient,code);
        return read(telnetClient,prompt + " ");
    }

    private String read(TelnetClient telnetClient,String code){
        char lastChar = code.charAt(code.length() - 1);
        StringBuffer stringBuffer = new StringBuffer();
        try {
            char ch = (char) is.read();

            while(true){
                stringBuffer.append(ch);
                if(ch == lastChar){
                    if(stringBuffer.toString().endsWith(code)){
                        Log.d("zouguo",stringBuffer.toString());
                        return stringBuffer.toString();
                    }
                }
                ch = (char) is.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void write(TelnetClient telnetClient,String code){
        try {
            ps.println(code);
            ps.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disConnection(){
        try {
            if(telnetClient != null)
                telnetClient.disconnect();
            if(is != null)
                is.close();
            if(ps != null)
                ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMsgToRouter(final int flag,final String msg){

        new Thread(new Runnable() {
            @Override
            public void run() {

                Socket socket = new Socket();
                InetSocketAddress inetSocketAddress = new InetSocketAddress("192.168.80.1",7888);
                try {
                    socket.connect(inetSocketAddress,5000);

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    pw.write(msg);
                    pw.flush();

                    socket.shutdownOutput();

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String infos = "";
                    String info = null;

                    while((info = br.readLine()) != null){
                        infos = infos + info;
                    }

                    //调用XML解析器
                    if(flag == 2){
                        //换行符丢失，需要对字符串格式化，否则XML解析失败
                        infos = infos.replace("PARTITIONindex","PARTITION index");
                    }

                    Log.d("zouguo",infos);
                    //格式纠正
                    if(flag == 4){
                        infos = infos.replace("APindex","AP index");
                        Log.d("zouguo","AP列表：" + infos);
                    }

                    parseXMLWithPull(flag,infos);

                    socket.close();
                    os.close();
                    pw.close();
                    is.close();
                    isr.close();
                    br.close();

                    Message msg = new Message();
                    msg.what = 8;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();

                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }

                isFresh = false;
            }
        }).start();
    }

    private void parseXMLWithPull(int flag,String xmlData){
        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();

            String routerIsRepeater = null;
            int APIndex = 1;
            String APSSID = null;
            int APSignal = 100;
            String APEncrypt = null;

            apindexList.clear();
            apssidList.clear();
            apsignalList.clear();
            apencryptList.clear();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        //开始解析某个节点
                        String nodeName = xmlPullParser.getName();

                        switch (flag){
                            case 3:
                                if("IS_ENABLE".equals(nodeName)){
                                    routerIsRepeater = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 15;
                                    msg.obj = routerIsRepeater;
                                    handler.sendMessage(msg);
                                }
                                break;
                            case 4:
                                if("AP".equals(nodeName)){
                                    APIndex = Integer.parseInt(xmlPullParser.getAttributeValue(0));
                                }
                                if("SSID".equals(nodeName)){
                                    APSSID = xmlPullParser.nextText();
                                }
                                if("SIGNAL".equals(nodeName)){
                                    APSignal = Integer.parseInt(xmlPullParser.nextText());
                                }
                                if("ENCRYPT_MODE".equals(nodeName)){
                                    APEncrypt = xmlPullParser.nextText();
                                }
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //完成解析某个节点
                        String node = xmlPullParser.getName();
                        if(flag == 4 && node.equals("AP")) {
                            //代表一个AP信息读取完成
                            apindexList.add(APIndex);
                            apssidList.add(APSSID);
                            apsignalList.add(APSignal);
                            //处理wifi加密方式
                            if (APEncrypt.equals("WPA2")) {
                                apencryptList.add("psk2");
                            } else if (APEncrypt.equals("WPA")) {
                                apencryptList.add("psk");
                            } else if (APEncrypt.startsWith("WPA+WPA2")) {
                                apencryptList.add("psk-mixed");
                            } else if (APEncrypt.equals("")) {
                                apencryptList.add("");
                            }else{
                                apencryptList.add("Other");
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }

            if(flag == 4){
                Message msg = new Message();
                msg.what = 6;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Message msg = new Message();
            msg.what = 7;
            handler.sendMessage(msg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
                &&event.getRepeatCount() == 0){
            finish();
        }
        return false;
    }
}
