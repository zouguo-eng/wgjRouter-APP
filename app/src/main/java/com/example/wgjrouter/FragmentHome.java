package com.example.wgjrouter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FragmentHome extends Fragment implements View.OnClickListener {

    private LinearLayout fg_home_ll_hollrouter;
    private LinearLayout fg_home_ll_superrouter;
    private LinearLayout fg_home_ll_help;
    private LinearLayout fg_home_ll_appsetting;
    private LinearLayout fg_home_ll_funny;
    private LinearLayout fg_home_ll_backup;
    private LinearLayout fg_home_ll_appabout;

    private TextView fg_home_tv_phone;
    private TextView fg_home_tv_ip;

    private Socket socket;

    private Handler handler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 2:
                    Toast.makeText(getActivity(),"抱歉，微路由通信超时",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fg_home,container,false);

        fg_home_ll_hollrouter = view.findViewById(R.id.fg_home_ll_hollrouter);
        fg_home_ll_superrouter = view.findViewById(R.id.fg_home_ll_superrouter);
        fg_home_ll_help = view.findViewById(R.id.fg_home_ll_help);
        fg_home_ll_appsetting = view.findViewById(R.id.fg_home_ll_appsetting);
        fg_home_ll_funny = view.findViewById(R.id.fg_home_ll_funny);
        fg_home_ll_backup = view.findViewById(R.id.fg_home_ll_backup);
        fg_home_ll_appabout = view.findViewById(R.id.fg_home_ll_appabout);

        fg_home_tv_phone = view.findViewById(R.id.fg_home_tv_phone);
        fg_home_tv_ip = view.findViewById(R.id.fg_home_tv_ip);

        fg_home_ll_hollrouter.setOnClickListener(this);
        fg_home_ll_superrouter.setOnClickListener(this);
        fg_home_ll_help.setOnClickListener(this);
        fg_home_ll_appsetting.setOnClickListener(this);
        fg_home_ll_funny.setOnClickListener(this);
        fg_home_ll_backup.setOnClickListener(this);
        fg_home_ll_appabout.setOnClickListener(this);

        initShow();
        getUserPhoneIP();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fg_home_ll_hollrouter:
                //首先监测与微路由网关能否通信
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket();
                            InetSocketAddress inetSocketAddress = new InetSocketAddress("192.168.80.1",7888);
                            socket.connect(inetSocketAddress,2000);

                            Intent goHollRouter = new Intent(getActivity(), SettingHollRouterActivity.class);
                            startActivity(goHollRouter);
                        } catch (IOException e) {
                            Message msg = new Message();
                            msg.what = 2;
                            handler.sendMessage(msg);
                        } finally {
                            try {
                                if(socket != null)
                                    socket.close();
                            } catch (IOException e) {
                                Message msg = new Message();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                        }
                    }
                }).start();
                break;
            case R.id.fg_home_ll_superrouter:
                Intent goSuperRouter = new Intent(getActivity(), SettingSuperRouterActivity.class);
                startActivity(goSuperRouter);
                break;
            case R.id.fg_home_ll_funny:
                Intent golive = new Intent(getActivity(),FunnyLiveActivity.class);
                startActivity(golive);
                break;
            case R.id.fg_home_ll_backup:
                Intent gobackup = new Intent(getActivity(), BackupListViewActivity.class);
                startActivity(gobackup);
                break;
            case R.id.fg_home_ll_help:
                Intent goHelp = new Intent(getActivity(), WebShow.class);
                goHelp.putExtra("web_title","帮助");
                goHelp.putExtra("web_url","https://docs.qq.com/doc/DUExxellidWpqaERE");
                startActivity(goHelp);
                break;
            case R.id.fg_home_ll_appsetting:
                Intent goSelfStyle = new Intent(getActivity(),SettingAppActivity.class);
                startActivity(goSelfStyle);
                break;
            case R.id.fg_home_ll_appabout:
                Intent goAbout = new Intent(getActivity(),AboutApp.class);
                startActivity(goAbout);
                break;
        }
    }

    /**
     * 设置手机型号、IP
     */
    private void initShow(){
        fg_home_tv_phone.setText(Build.MODEL);
    }

    /**
     * 获取当前设备名称、IP
     */
    private void getUserPhoneIP(){

    }
}
