package com.example.wgjrouter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.example.consts.Consts;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class SettingHollRouterFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Preference wifi_setwifi;
    private Preference wifi_repeater;
    private Preference router_initweb;
    private Preference router_devices;
    private Preference router_state;
    private Preference router_shutdown;
    private Preference router_restore;

    private TelnetClient telnetClient = null;
    private InputStream is = null;
    private PrintStream ps = null;
    private char prompt = '$';

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.hollrouter_setting_preference);

        wifi_setwifi = findPreference("wifi_setwifi");
        wifi_repeater = findPreference("wifi_repeater");
        router_initweb = findPreference("router_initwan");
        router_devices = findPreference("router_devices");
        router_state = findPreference("router_state");
        router_shutdown = findPreference("router_shutdown");
        router_restore = findPreference("router_restore");

        wifi_setwifi.setOnPreferenceClickListener(this);
        wifi_repeater.setOnPreferenceClickListener(this);
        router_initweb.setOnPreferenceClickListener(this);
        router_devices.setOnPreferenceClickListener(this);
        router_state.setOnPreferenceClickListener(this);
        router_shutdown.setOnPreferenceClickListener(this);
        router_restore.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "wifi_setwifi":
                Intent routerSetPwd = new Intent(getActivity(),RouterSetWiFiPwd.class);
                startActivity(routerSetPwd);
                break;
            case "wifi_repeater":
                Intent routerSetRepeater = new Intent(getActivity(),RouterSetRepeater.class);
                startActivity(routerSetRepeater);
                break;
            case "router_initwan":
//                Intent routerSetWan = new Intent(getActivity(),RouterSetWan.class);
//                startActivity(routerSetWan);
                Toast.makeText(getActivity(),"暂不可用",Toast.LENGTH_SHORT).show();
                break;
            case "router_devices":
                Intent routerDevices = new Intent(getActivity(),RouterDevices.class);
                startActivity(routerDevices);
                break;
            case "router_state":
                Intent routerState = new Intent(getActivity(),RouterStatus.class);
                startActivity(routerState);
                break;
            case "router_shutdown":
                //警告提示框
                AlertDialog ad = null;
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("微路由关机提醒");
                adb.setMessage("关机后无法远程开机，是否关机?");
                adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        connectRouter(0,"root","holl0311");

                        Toast.makeText(getActivity(),"拜了个拜",Toast.LENGTH_SHORT).show();
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
                ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;
            case "router_restore":
                //警告提示框
                AlertDialog adr = null;
                AlertDialog.Builder adbr = new AlertDialog.Builder(getActivity());
                adbr.setTitle("微路由初始化提醒");
                adbr.setMessage("初始化后将清空微路由的所有配置");
                adbr.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        connectRouter(1,"root","holl0311");

                        Toast.makeText(getActivity(),"初始化完成，请重连热点",Toast.LENGTH_SHORT).show();
                    }
                });
                adbr.setNegativeButton("算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                adr = adbr.create();
                adr.show();
                adr.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                adr.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;
        }
        return false;
    }

    //连接到路由器
    private void connectRouter(final int flag, final String user, final String password){
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
                            //关机
                            sendCodeToRouter(telnetClient,Consts.SHUTDOWN_ROUTER_BYSH);
                            break;
                        case 1:
                            //恢复出厂设置
                            sendCodeToRouter(telnetClient,Consts.RESTORE_ROUTER_BYSH);
                            break;
                    }

                    //退出
                    ps.println("exit\r\n");
                    ps.flush();
                    disConnection();
                }catch (Exception e) {
                    e.printStackTrace();
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
}
