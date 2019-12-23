package com.example.wgjrouter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingSuperRouterFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private ProgressBar setting_superrouter_pb;

    private EditTextPreference gateway_config;
    private EditTextPreference penetration_config;
    private EditTextPreference link_diskdir_config;
    private CheckBoxPreference penetration_https_config;
    private EditTextPreference aria2_dir_config;

    private Preference remote_admin_watch;
    private Preference remote_file_watch;
    private Preference remote_download;
    private Preference local_download;
    private Preference get_bttrackerlist;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setting_superrouter_pb.setVisibility(View.GONE);
                }
            });

            switch (msg.what){
                case 0:
                    Toast.makeText(getActivity(),"抱歉，获取Aria2-BT服务器地址失败",Toast.LENGTH_SHORT).show();
                    break;
                case 128:
                    String formatList = msg.obj.toString().replace("\n\n",",");
                    formatList = formatList.substring(0,formatList.length() - 1);

                    ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData cd = ClipData.newPlainText(null,formatList);
                    cm.setPrimaryClip(cd);

                    Toast.makeText(getActivity(),"已复制到剪贴板",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.superrouter_setting_preference);

        setting_superrouter_pb = getActivity().findViewById(R.id.setting_superrouter_pb);

        gateway_config = (EditTextPreference) findPreference("gateway_config");
        penetration_config = (EditTextPreference) findPreference("penetration_config");
        link_diskdir_config = (EditTextPreference) findPreference("link_diskdir_config");
        penetration_https_config = (CheckBoxPreference) findPreference("penetration_https_config");
        aria2_dir_config = (EditTextPreference) findPreference("aria2_dir_config");

        remote_admin_watch = findPreference("remote_admin_watch");
        remote_file_watch = findPreference("remote_file_watch");
        local_download = findPreference("local_download");
        remote_download = findPreference("remote_download");
        get_bttrackerlist = findPreference("get_bttrackerlist");

        remote_admin_watch.setOnPreferenceClickListener(this);
        remote_file_watch.setOnPreferenceClickListener(this);
        local_download.setOnPreferenceClickListener(this);
        remote_download.setOnPreferenceClickListener(this);
        get_bttrackerlist.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "remote_admin_watch":
                String localOutUrl = penetration_config.getText();
                if(localOutUrl != null && localOutUrl != ""){
                    if(!localOutUrl.startsWith("http")){
                        if(penetration_https_config != null && penetration_https_config.isChecked()){
                            localOutUrl = "https://" + localOutUrl + "/";
                        }else{
                            localOutUrl = "http://" + localOutUrl + "/";
                        }
                    }
                    Intent goRemoteAdmin = new Intent(getActivity(),WebShow.class);
                    goRemoteAdmin.putExtra("web_showtitle",false);
                    goRemoteAdmin.putExtra("web_title","路由器Web管理");
                    goRemoteAdmin.putExtra("web_url",localOutUrl);
                    startActivity(goRemoteAdmin);
                }else{
                    Toast.makeText(getActivity(),"内网穿透未配置",Toast.LENGTH_SHORT).show();
                }
                break;
            case "remote_file_watch":
                String fileUrl = penetration_config.getText();
                String linkDir = link_diskdir_config.getText();
                if(fileUrl != null && fileUrl != "" && linkDir != null && linkDir != ""){
                    if(!fileUrl.startsWith("http")){
                        if(penetration_https_config != null && penetration_https_config.isChecked()){
                            fileUrl = "https://" + fileUrl;
                        }else{
                            fileUrl = "http://" + fileUrl;
                        }
                    }

                    Intent goFileAdmin = new Intent(getActivity(),RemoteFileActivity.class);
                    goFileAdmin.putExtra("web_host",fileUrl);
                    goFileAdmin.putExtra("web_url",fileUrl + "/" + linkDir + "/");
                    startActivity(goFileAdmin);
                }else{
                    Toast.makeText(getActivity(),"内网穿透、远程根目录未配置",Toast.LENGTH_SHORT).show();
                }
                break;
            case "local_download":
                String gateway = gateway_config.getText();
                String aria2Dir = aria2_dir_config.getText();

                if(gateway != null && gateway != ""){
                    if(!gateway.startsWith("http")){
                        if(penetration_https_config != null && penetration_https_config.isChecked()){
                            gateway = "https://" + gateway + "/";
                        }else{
                            gateway = "http://" + gateway + "/";
                        }
                    }

                    if(aria2Dir != null && aria2Dir != ""){
                        Intent goAria2LocalWeb = new Intent(getActivity(),WebShow.class);
                        goAria2LocalWeb.putExtra("web_showtitle",false);
                        goAria2LocalWeb.putExtra("web_showback",true);
                        goAria2LocalWeb.putExtra("web_title","内网AriaNg下载器");
                        goAria2LocalWeb.putExtra("web_url",gateway + aria2Dir);
                        startActivity(goAria2LocalWeb);
                    }else{
                        Toast.makeText(getActivity(),"Aria2 Web目录未配置",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"网关未配置",Toast.LENGTH_SHORT).show();
                }
                break;
            case "remote_download":
                String pentUrl = penetration_config.getText();
                String aria2Dirr = aria2_dir_config.getText();

                if(pentUrl != null && pentUrl != ""){
                    if(!pentUrl.startsWith("http")){
                        if(penetration_https_config != null && penetration_https_config.isChecked()){
                            pentUrl = "https://" + pentUrl + "/";
                        }else{
                            pentUrl = "http://" + pentUrl + "/";
                        }
                    }

                    if(aria2Dirr != null && aria2Dirr != ""){
                        Intent goAria2RemoteWeb = new Intent(getActivity(),WebShow.class);
                        goAria2RemoteWeb.putExtra("web_showtitle",false);
                        goAria2RemoteWeb.putExtra("web_showback",true);
                        goAria2RemoteWeb.putExtra("web_title","外网AriaNg下载器");
                        goAria2RemoteWeb.putExtra("web_url",pentUrl + aria2Dirr);
                        startActivity(goAria2RemoteWeb);
                    }else{
                        Toast.makeText(getActivity(),"Aria2 Web目录名未配置",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"内网穿透未配置",Toast.LENGTH_SHORT).show();
                }
                break;
            case "get_bttrackerlist":
                setting_superrouter_pb.setVisibility(View.VISIBLE);

                OkHttpClient ohc = new OkHttpClient();
                Request req = new Request.Builder().url("https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_best.txt")
                        .method("GET",null)
                        .build();
                Call call = ohc.newCall(req);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Message msg = new Message();
                        msg.what = 128;
                        msg.obj = response.body().string();
                        handler.sendMessage(msg);
                    }
                });
                break;
        }
        return false;
    }
}
