package com.example.wgjrouter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.consts.Consts;

import org.apache.commons.net.telnet.TelnetClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RouterSetWiFiPwd extends AppCompatActivity {

    private ProgressBar router_setwifipwd_pb;
    private Toolbar router_setwifipwd_tb;
    private Button router_setwifipwd_set;
    private EditText router_setwifipwd_ssid_et;
    private EditText router_setwifipwd_key_et;
    private EditText router_setwifipwd_confirm_et;

    private TelnetClient telnetClient = null;
    private InputStream is = null;
    private PrintStream ps = null;
    private char prompt = '$';

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    router_setwifipwd_pb.setVisibility(View.GONE);
                }
            });

            switch (msg.what){
                case 0:
                    Toast.makeText(RouterSetWiFiPwd.this,"抱歉，与微路由通信超时",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    router_setwifipwd_ssid_et.setText(msg.obj.toString());
                    break;
                case 9:
                    Toast.makeText(RouterSetWiFiPwd.this,"恭喜，WiFi配置成功，请重连热点",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_setwifipwd);

        initView();
        getWifiInfo(2,Consts.ROUTER_GET_INFOS);
    }

    private void initView(){
        router_setwifipwd_pb = findViewById(R.id.router_setwifipwd_pb);
        router_setwifipwd_tb = findViewById(R.id.router_setwifipwd_tb);
        router_setwifipwd_tb.setNavigationIcon(R.mipmap.back);
        router_setwifipwd_tb.setTitle("微路由WiFi设置");

        router_setwifipwd_set = findViewById(R.id.router_setwifipwd_set);

        router_setwifipwd_ssid_et = findViewById(R.id.router_setwifipwd_ssid_et);
        router_setwifipwd_key_et = findViewById(R.id.router_setwifipwd_key_et);
        router_setwifipwd_confirm_et = findViewById(R.id.router_setwifipwd_confirm_et);

        router_setwifipwd_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!router_setwifipwd_ssid_et.getText().toString().equals("")){
                    if(!router_setwifipwd_key_et.getText().toString().equals("")){
                        if(!router_setwifipwd_confirm_et.getText().toString().equals("")){
                            if(router_setwifipwd_confirm_et.getText().toString().equals(router_setwifipwd_key_et.getText().toString())){
                                Toast.makeText(RouterSetWiFiPwd.this,"WiFi信息配置中，请稍等",Toast.LENGTH_SHORT).show();
                                router_setwifipwd_pb.setVisibility(View.VISIBLE);

                                //设置WiFi
                                connectRouter("root","holl0311",router_setwifipwd_ssid_et.getText().toString().replaceAll("\\s*",""),router_setwifipwd_confirm_et.getText().toString().replaceAll("\\s*",""));
                            }else{
                                router_setwifipwd_pb.setVisibility(View.GONE);
                                Toast.makeText(RouterSetWiFiPwd.this,"两次密码不一致",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

        router_setwifipwd_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getWifiInfo(final int flag,final String code){
        //初始获取当前WiFi的名称、密码
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    InetSocketAddress isa = new InetSocketAddress("192.168.80.1",7888);
                    socket.connect(isa,5000);

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    pw.write(code);
                    pw.flush();

                    socket.shutdownOutput();

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String info = null;
                    String infos = "";

                    while((info = br.readLine()) != null){
                        infos = infos + info;
                    }

                    //调用XML解析器
                    if(flag == 2){
                        //换行符丢失，需要对字符串格式化，否则XML解析失败
                        infos = infos.replace("PARTITIONindex","PARTITION index");
                    }
                    parseXMLWithPull(flag,infos);

                    socket.close();
                    os.close();
                    pw.close();
                    is.close();
                    isr.close();
                    br.close();
                } catch (IOException e) {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }

            }
        }).start();
    }

    private void parseXMLWithPull(int flag,String xmlData) {
        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();

            String apSSID = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //开始解析某个节点
                        String nodeName = xmlPullParser.getName();

                        switch (flag) {
                            case 2:
                                if ("SSID".equals(nodeName)) {
                                    apSSID = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = apSSID;
                                    handler.sendMessage(msg);
                                }
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //完成解析某个节点
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectRouter(final String user,final String password,final String SSID,final String KEY){
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

                    //设置WIFI
                    sendCodeToRouter(telnetClient,Consts.CHANGE_WIFI_SSID.replace("**",SSID));
                    sendCodeToRouter(telnetClient,Consts.CHANGE_WIFI_KEY.replace("**",KEY));
                    sendCodeToRouter(telnetClient,Consts.CHANGE_WIFI_ENCR);
                    sendCodeToRouter(telnetClient,Consts.CHANGE_WIFI_COMMIT);

                    //退出
                    ps.println("exit\r\n");
                    ps.flush();
                    disConnection();

                    Message msg = new Message();
                    msg.what = 9;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();

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

    private void sendCodeToRouter(TelnetClient telnetClient,String code){
        write(telnetClient,code);
        read(telnetClient,prompt + " ");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
        &&event.getRepeatCount() == 0){
            finish();
        }
        return false;
    }
}
