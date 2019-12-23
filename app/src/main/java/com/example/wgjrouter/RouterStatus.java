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
import android.widget.TextView;
import android.widget.Toast;

import com.example.consts.Consts;

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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class RouterStatus extends AppCompatActivity {

    private Toolbar router_status_tb;
    private TextView router_status_tv_devices;
    private TextView router_status_tv_mode;
    private TextView router_status_tv_ip;
    private TextView router_status_tv_subnetmask;
    private TextView router_status_tv_gateway;
    private TextView router_status_tv_dns;
    private TextView router_status_tv_isrepeater;
    private TextView router_status_tv_product;
    private TextView router_status_tv_hardwareversion;
    private TextView router_status_tv_firmwareversion;
    private TextView router_status_tv_ssid;
    private TextView router_status_tv_encrypt;
    private TextView router_status_tv_wiremac;
    private TextView router_status_tv_wirelessmac;
    private TextView router_status_tv_partition_number;
    private TextView router_status_tv_partition_type;
    private TextView router_status_tv_disktotalsize;
    private TextView router_status_tv_disklastsize;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(RouterStatus.this,"抱歉，与微路由通信超时",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    router_status_tv_devices.setText(msg.obj.toString() + "台");
                    break;
                case 2:
                    router_status_tv_hardwareversion.setText(msg.obj.toString());
                    break;
                case 3:
                    router_status_tv_firmwareversion.setText(msg.obj.toString());
                    break;
                case 4:
                    router_status_tv_product.setText(msg.obj.toString());
                    break;
                case 5:
                    router_status_tv_ssid.setText(msg.obj.toString());
                    break;
                case 6:
                    router_status_tv_encrypt.setText(msg.obj.toString());
                    break;
                case 7:
                    router_status_tv_wirelessmac.setText(msg.obj.toString());
                    break;
                case 8:
                    router_status_tv_wiremac.setText(msg.obj.toString());
                    break;
                case 9:
                    List<String> list = new ArrayList<>();
                    list = (List<String>) msg.obj;

                    router_status_tv_partition_number.setText(list.get(0));
                    router_status_tv_partition_type.setText(list.get(1));
                    router_status_tv_disktotalsize.setText(list.get(2));
                    router_status_tv_disklastsize.setText(list.get(3));
                    break;
                case 10:
                    router_status_tv_mode.setText(msg.obj.toString());
                    break;
                case 11:
                    router_status_tv_ip.setText(msg.obj.toString());
                    break;
                case 12:
                    router_status_tv_subnetmask.setText(msg.obj.toString());
                    break;
                case 13:
                    router_status_tv_gateway.setText(msg.obj.toString());
                    break;
                case 14:
                    router_status_tv_dns.setText(msg.obj.toString());
                    break;
                case 15:
                    router_status_tv_isrepeater.setText(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_status);

        initView();

        //获取连接终端
        sendMsgToRouter(0,Consts.ROUTER_ON_DEVICES);
        //获取硬件版本、固件版本
        sendMsgToRouter(1,Consts.ROUTER_GET_VERSION);
        //获取硬盘、WIFI、MAC信息
        sendMsgToRouter(2,Consts.ROUTER_GET_INFOS);
        //获取路由信息
        sendMsgToRouter(3,Consts.ROUTER_GET_INFO);

    }

    private void initView(){
        router_status_tb = findViewById(R.id.router_status_tb);
        router_status_tb.setNavigationIcon(R.mipmap.back);
        router_status_tb.setTitle("路由器状态");

        router_status_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        router_status_tv_devices = findViewById(R.id.router_status_tv_devices);
        router_status_tv_mode = findViewById(R.id.router_status_tv_mode);
        router_status_tv_ip = findViewById(R.id.router_status_tv_ip);
        router_status_tv_subnetmask = findViewById(R.id.router_status_tv_subnetmask);
        router_status_tv_gateway = findViewById(R.id.router_status_tv_gateway);
        router_status_tv_dns = findViewById(R.id.router_status_tv_dns);
        router_status_tv_isrepeater = findViewById(R.id.router_status_tv_isrepeater);
        router_status_tv_product = findViewById(R.id.router_status_tv_product);
        router_status_tv_hardwareversion = findViewById(R.id.router_status_tv_hardwareversion);
        router_status_tv_firmwareversion = findViewById(R.id.router_status_tv_firmwareversion);
        router_status_tv_ssid = findViewById(R.id.router_status_tv_ssid);
        router_status_tv_encrypt = findViewById(R.id.router_status_tv_encrypt);
        router_status_tv_wiremac = findViewById(R.id.router_status_tv_wiremac);
        router_status_tv_wirelessmac = findViewById(R.id.router_status_tv_wirelessmac);
        router_status_tv_partition_number = findViewById(R.id.router_status_tv_partition_number);
        router_status_tv_partition_type = findViewById(R.id.router_status_tv_partition_type);
        router_status_tv_disktotalsize = findViewById(R.id.router_status_tv_disktotalsize);
        router_status_tv_disklastsize = findViewById(R.id.router_status_tv_disklastsize);
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

    private void parseXMLWithPull(int flag,String xmlData){
        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();

            String devicesNum = null;
            String routerMode = null;
            String routerIP = null;
            String routerSubnetMask = null;
            String routerGateway = null;
            String routerDNS = null;
            String routerIsRepeater = null;
            String product = null;
            String hardwareV = null;
            String firmwareV = null;
            String apSSID = null;
            String apEncrypt = null;
            String wireMac = null;
            String wirelessMac = null;
            String diskPartition = null;
            String diskPType = null;
            String diskTotalSize = null;
            String diskLastSize = null;

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        //开始解析某个节点
                        String nodeName = xmlPullParser.getName();

                        switch (flag){
                            case 0:
                                if("DEVICE_NUM".equals(nodeName)){
                                    devicesNum = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = devicesNum;
                                    handler.sendMessage(msg);
                                }
                                break;
                            case 1:
                                if("CUR_VER".equals(nodeName)){
                                    hardwareV = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = hardwareV;
                                    handler.sendMessage(msg);
                                }
                                if("FW".equals(nodeName)){
                                    firmwareV = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 3;
                                    msg.obj = firmwareV;
                                    handler.sendMessage(msg);
                                }
                                if("NAME".equals(nodeName)){
                                    product = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 4;
                                    msg.obj = product;
                                    handler.sendMessage(msg);
                                }
                                break;
                            case 2:
                                if("SSID".equals(nodeName)){
                                    apSSID = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 5;
                                    msg.obj = apSSID;
                                    handler.sendMessage(msg);
                                }
                                if("ENCRYPTION".equals(nodeName)){
                                    apEncrypt = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 6;
                                    msg.obj = apEncrypt;
                                    handler.sendMessage(msg);
                                }
                                if("ETH0_MAC".equals(nodeName)){
                                    wirelessMac = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 7;
                                    msg.obj = wirelessMac;
                                    handler.sendMessage(msg);
                                }
                                if("WAN0_MAC".equals(nodeName)){
                                    wireMac = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 8;
                                    msg.obj = wireMac;
                                    handler.sendMessage(msg);
                                }
                                if("PARTITION".equals(nodeName)){
                                    diskPartition = xmlPullParser.getAttributeValue(0);
                                    diskPType = xmlPullParser.getAttributeValue(1);
                                    diskTotalSize = xmlPullParser.getAttributeValue(2);
                                    diskLastSize = xmlPullParser.getAttributeValue(3);

                                    List<String> list = new ArrayList<>();
                                    list.add(diskPartition);
                                    list.add(diskPType);
                                    list.add(diskTotalSize);
                                    list.add(diskLastSize);

                                    Message msg = new Message();
                                    msg.what = 9;
                                    msg.obj = list;
                                    handler.sendMessage(msg);
                                }
                                break;
                            case 3:
                                if("ROUTE_INFO".equals(nodeName)){
                                    routerMode = xmlPullParser.getAttributeValue(0);

                                    Message msg = new Message();
                                    msg.what = 10;
                                    msg.obj = routerMode;
                                    handler.sendMessage(msg);
                                }
                                if("IP".equals(nodeName)){
                                    routerIP = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 11;
                                    msg.obj = routerIP;
                                    handler.sendMessage(msg);
                                }
                                if("MASK".equals(nodeName)){
                                    routerSubnetMask = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 12;
                                    msg.obj = routerSubnetMask;
                                    handler.sendMessage(msg);
                                }
                                if("GATEWAY".equals(nodeName)){
                                    routerGateway = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 13;
                                    msg.obj = routerGateway;
                                    handler.sendMessage(msg);
                                }
                                if("DNS".equals(nodeName)){
                                    routerDNS = xmlPullParser.nextText();

                                    Message msg = new Message();
                                    msg.what = 14;
                                    msg.obj = routerDNS;
                                    handler.sendMessage(msg);
                                }
                                if("IS_ENABLE".equals(nodeName)){
                                    if(xmlPullParser.nextText().equals("1")){
                                        //中继已开启
                                        routerIsRepeater = "已开启";
                                    }else{
                                        routerIsRepeater = "无";
                                    }

                                    Message msg = new Message();
                                    msg.what = 15;
                                    msg.obj = routerIsRepeater;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
                &&event.getRepeatCount() == 0){
            finish();
        }
        return false;
    }
}
