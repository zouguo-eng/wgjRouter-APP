package com.example.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class MyPhoneUtils {
    private static MyPhoneUtils myPhoneUtils;
    private Context context;

    private MyPhoneUtils(Context context){
        this.context = context;
    }

    public static MyPhoneUtils getInstance(Context context){
        if(myPhoneUtils == null){
            myPhoneUtils = new MyPhoneUtils(context);
        }
        return myPhoneUtils;
    }

    /**
     * 检查网络状态
     * @return
     */
    public boolean checkNetworkState(){
        boolean networkState = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()){
                networkState = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return networkState;
    }

    /**
     * 检查网络类型
     * @return
     */
    public int checkNetworkType(){
        /**
         * 0--其它
         * 1--GPRS
         * 2--WiFi
         */
        int networkType = 0;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State gprs = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        if(gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING){
            networkType = 1;
        }else if(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING){
            networkType = 2;
        }

        return networkType;
    }

    public void getNetworkGateway(){

    }

    public String getNetworkLocalIP(){
        String localIp = null;

        switch (checkNetworkType()){
            case 0:
                //未知
                break;
            case 1:
                //GPRS
                try {
                    ArrayList<NetworkInterface> alni = Collections.list(NetworkInterface.getNetworkInterfaces());
                    for(NetworkInterface ni : alni){
                        ArrayList<InetAddress> alia = Collections.list(ni.getInetAddresses());
                        for(InetAddress ia : alia){
                            if(!ia.isLoopbackAddress() && !ia.isLinkLocalAddress()){
                                localIp = ia.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                //WIFI
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                if(wm.isWifiEnabled()){
                    //WIFI已启用
                    WifiInfo wi = wm.getConnectionInfo();

                    localIp = intIpToString(wi.getIpAddress());
                }
                break;
        }
        return localIp;
    }

    public void getNetworkInternetIp(){

    }

    private String intIpToString(int intIp){
        return (intIp & 0xFF) + "." +
               ((intIp >> 8) & 0xFF) + "." +
               ((intIp >> 16) & 0xFF) + "." +
               (intIp >> 24 & 0xFF);
    }

    public boolean isInputMethodShow(){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    /**
     * 隐藏输入法
     * @param view
     */
    public void hideInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm == null){
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    /**
     * 显示输入法
     * @param view
     */
    public void showInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm == null){
            return;
        }
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);
    }

    /**
     *调用其它视频播放器播放网络视频
     * 不需要7.0版本判断
     * @param vNetUrl
     */
    public void usePlayerPlayNetVideo(String vNetUrl){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(vNetUrl),"video/*");
        context.startActivity(intent);
    }

    /**
     * 调用其它工具解析文件
     * @param localUrl
     * @param mimeType
     * mimeType:
     * 1、video/*，2、audio/*，3、application/msword，4、application/vnd.ms-excel，5、application/vnd.ms-powerpoint
     * 6、application/vnd.ms-works，7、text/plain，8、application/x-compress，9、application/x-zip-compressed
     * 10、application/x-compressed，11、application/x-tar，12、image/，13、application/pdf
     */
    public boolean useLocalResolver(String localUrl,String mimeType){
        Log.d("zouguo","localUrl1:" + localUrl);
        Log.d("zouguo","mimeType1:" + mimeType);

        boolean resolveOk = false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(localUrl);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                //7.0以上版本
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context,context.getPackageName() + ".FileProvider",file);
                intent.setDataAndType(contentUri,mimeType);
            }else{
                Uri uri = Uri.fromFile(file);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri,mimeType);
            }
            resolveOk = true;
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resolveOk;
    }

    /**
     * 调用系统分享文件
     * @param localUrl
     * @param mimeType
     */
    public void useLocalSharer(String localUrl,String mimeType){
        Log.d("zouguo","localUrl2:" + localUrl);
        Log.d("zouguo","mimeType2:" + mimeType);

        Intent intent = new Intent(Intent.ACTION_SEND);
        File file = new File(localUrl);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            //7.0以上版本
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context,context.getPackageName() + ".FileProvider",file);
            intent.putExtra(Intent.EXTRA_STREAM,contentUri);
            intent.setType(mimeType);
        }else{
            Uri uri = Uri.fromFile(file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.setType(mimeType);
        }
        context.startActivity(Intent.createChooser(intent,"分享到:"));
    }

    /**
     * 检查某个应用是否已安装
     * @param packName
     */
    public boolean checkIsInstalledApp(String packName){
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(packName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi != null;
    }

    /**
     * 通过包名唤醒APP
     * 迅雷com.xunlei.downloadprovider
     * @param packName
     */
    public void wakeAppByPackName(String packName){
        if(!checkIsInstalledApp(packName)){
            return;
        }

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packName);

        if(intent != null){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }

    /**
     * 通过包名、活动名唤醒APP
     * @param packName
     * @param actiName
     */
    public void wakeAppByPackNameAndActivity(String packName,String actiName){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName cn = new ComponentName(packName,actiName);
        intent.setComponent(cn);
        context.startActivity(intent);
    }

    /**
     * 通过特定类型的Url唤醒APP
     * @param givenUrl
     */
    public void wakeAppByUrl(String givenUrl){
        Intent intent = new Intent();
        intent.setData(Uri.parse(givenUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 安装apk
     * @param apkUrl
     */
    public void installApk(String apkUrl){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(apkUrl);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            //7.0以上
            Uri conUri = FileProvider.getUriForFile(context,context.getPackageName() + ".FileProvider",file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(conUri,"application/vnd.android.package-archive");
        }else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri,"application/vnd.android.package-archive");
        }

        context.startActivity(intent);
    }
}
