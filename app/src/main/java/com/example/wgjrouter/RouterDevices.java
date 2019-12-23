package com.example.wgjrouter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adapter.MyBaLvDevicesAdater;
import com.example.bean.MyBaLvDevices;
import com.example.consts.Consts;

import org.apache.commons.net.telnet.TelnetClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RouterDevices extends AppCompatActivity {

    private Toolbar router_devices_tb;
    private ListView router_devices_lv;

    private List<String> devNameList = new ArrayList<>();
    private List<String> devIPList = new ArrayList<>();
    private List<String> devMACList = new ArrayList<>();
    private List<String> devAccNetList = new ArrayList<>();
    private List<String> devAccShaList = new ArrayList<>();
    private List<String> devAccBindList = new ArrayList<>();

    private List<MyBaLvDevices> myBaLvDevicesList;
    private MyBaLvDevicesAdater myBaLvDevicesAdater;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(RouterDevices.this,"抱歉，获取在线设备异常",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    myBaLvDevicesList.clear();

                    if(devNameList.size() == 0){
                        Toast.makeText(RouterDevices.this,"当前无在线设备",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for(int i = 0;i<devNameList.size();i++){
                        MyBaLvDevices myBaLvDevices = new MyBaLvDevices();
                        myBaLvDevices.setDevName(devNameList.get(i));
                        myBaLvDevices.setDevIP(devIPList.get(i));
                        myBaLvDevices.setDevMac(devMACList.get(i));
                        myBaLvDevices.setDevAccNet(devAccNetList.get(i));
                        myBaLvDevices.setDevAccSha(devAccShaList.get(i));
                        myBaLvDevices.setDevBindMAC(devAccBindList.get(i));

                        myBaLvDevicesList.add(myBaLvDevices);
                    }

                    myBaLvDevicesAdater.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_devices);

        initView();
        getRouterDevices(Consts.ROUTER_ON_DEVICES);
    }

    private void initView(){
        router_devices_tb = findViewById(R.id.router_devices_tb);
        router_devices_tb.setNavigationIcon(R.mipmap.back);
        router_devices_tb.setTitle("接入设备权限");

        myBaLvDevicesList = new ArrayList<>();
        myBaLvDevicesAdater = new MyBaLvDevicesAdater(this,myBaLvDevicesList);
        router_devices_lv = findViewById(R.id.router_devices_lv);
        router_devices_lv.setAdapter(myBaLvDevicesAdater);

        router_devices_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getRouterDevices(final String code){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    InetSocketAddress isa = new InetSocketAddress(Consts.SOCKETHOST,Consts.SOCKETPORT);
                    socket.connect(isa);

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

                    Log.d("zouguo",infos);
                    parseXMLWithPull(0,infos);

                    socket.close();
                    os.close();
                    pw.close();
                    is.close();
                    isr.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseXMLWithPull(int flag,String xmlData){
        try {
            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = xppf.newPullParser();
            xpp.setInput(new StringReader(xmlData));

            int eventType = xpp.getEventType();

            String devName = null;
            String devIP = null;
            String devMac = null;
            String devAccNet = null;
            String devAccSha = null;
            String devBindMac = null;

            devNameList.clear();
            devIPList.clear();
            devMACList.clear();
            devAccNetList.clear();
            devAccShaList.clear();
            devAccBindList.clear();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        String nodeName = xpp.getName();

                        switch (flag){
                            case 0:
                                if("NAME".equals(nodeName)){
                                    devName = xpp.nextText();
                                }
                                if("IP".equals(nodeName)){
                                    devIP = xpp.nextText();
                                }
                                if("MAC".equals(nodeName)){
                                    devMac = xpp.nextText();
                                }
                                if("ACCESS_INTERNET".equals(nodeName)){
                                    devAccNet = xpp.nextText();
                                }
                                if("ACCESS_SHAREDISK".equals(nodeName)){
                                    devAccSha = xpp.nextText();
                                }
                                if("BIND_MAC".equals(nodeName)){
                                    devBindMac = xpp.nextText();
                                }
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String node = xpp.getName();

                        if(flag == 0 && "DEVICE".equals(node)){
                            //完成了一个设备的信息读取
                            devNameList.add(devName);
                            devIPList.add(devIP);
                            devMACList.add(devMac);
                            devAccNetList.add(devAccNet);
                            devAccShaList.add(devAccSha);
                            devAccBindList.add(devBindMac);
                        }
                        break;
                }

                eventType = xpp.next();
            }

            if(flag == 0){
                //设备信息读取完成
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Message msg = new Message();
            msg.what = 0;
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
