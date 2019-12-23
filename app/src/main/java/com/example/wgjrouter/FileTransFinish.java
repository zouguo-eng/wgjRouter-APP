package com.example.wgjrouter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MyRecTransAdapter;
import com.example.bean.TaskInfo;
import com.example.database.FileTrans;
import com.example.utils.MyPhoneUtils;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileTransFinish extends AppCompatActivity {

    private LinearLayout file_trans_finish_parentview;
    private RecyclerView file_trans_finish_rv;
    private Toolbar file_trans_finish_tb;
    private List<TaskInfo> lvFileTransList = null;
    private MyRecTransAdapter myRecTransAdapter = null;

    private List<Integer> updownList = new ArrayList<>();

    private String installApkUrl = "";
    private static int REQUEST_INSTALL_PERMISSION_CODE = 15926;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_trans_finish);

        file_trans_finish_parentview = findViewById(R.id.file_trans_finish_parentview);
        file_trans_finish_tb = findViewById(R.id.file_trans_finish_tb);

        //Toolbar设置菜单布局
        file_trans_finish_tb.inflateMenu(R.menu.file_finish_option_menu);

        file_trans_finish_tb.setNavigationIcon(R.mipmap.back);
        file_trans_finish_tb.setTitle("已完成");
        file_trans_finish_tb.setTitleTextColor(Color.WHITE);
        file_trans_finish_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        file_trans_finish_tb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.trans_finish_onlydown:
                        //仅显示下载记录
                        getTransDataFromDBOnlyDown();
                        break;
                    case R.id.trans_finish_clear:
                        LitePal.deleteAll(FileTrans.class);
                        //刷新
                        getTransDataFromDB();
                        break;
                    case R.id.trans_finish_deleteall:
                        //风险操作，提醒
                        AlertDialog adf = null;
                        AlertDialog.Builder adbf = new AlertDialog.Builder(FileTransFinish.this);
                        adbf.setTitle("删空提醒");
                        adbf.setMessage("是否删除已下载的全部文件");
                        adbf.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                //本地下载路径
                                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                    File sumFile = new File(file.getPath() + "/com.example.wgjrouter");

                                    if(sumFile.exists()){
                                        //目录存在
                                        Log.d("zouguo","文件数：" + sumFile.list().length);
                                        String[] lists = sumFile.list();

                                        for(int i = 0;i < lists.length;i++){
                                            Log.d("zouguo","文件：" + lists[i]);

                                            FileTrans ft = LitePal.where("filename = ? and upordown = ?",lists[i],"1").findLast(FileTrans.class);

                                            if(ft != null) {
                                                File f = new File(ft.getFilePath());

                                                if (f.exists()) {
                                                    f.delete();
                                                }
                                            }
                                        }

                                        //清空记录
                                        LitePal.deleteAll(FileTrans.class);
                                        //刷新
                                        getTransDataFromDB();

                                        Toast.makeText(FileTransFinish.this,"删空完成",Toast.LENGTH_SHORT).show();
                                    }
                                }
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
                        break;
                }
                return false;
            }
        });

        file_trans_finish_rv = findViewById(R.id.file_trans_finish_rv);

        LinearLayoutManager llm = new LinearLayoutManager(FileTransFinish.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        file_trans_finish_rv.setLayoutManager(llm);

        lvFileTransList = new ArrayList<>();
        myRecTransAdapter = new MyRecTransAdapter(FileTransFinish.this,lvFileTransList);
        file_trans_finish_rv.setAdapter(myRecTransAdapter);

        myRecTransAdapter.setOnItemListener(new MyRecTransAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(updownList.get(position) == 0){
                    //上传类型
                    Toast.makeText(FileTransFinish.this,"抱歉，暂不支持上传文件的查看",Toast.LENGTH_SHORT).show();
                    return;
                }

                TextView tv = view.findViewById(R.id.fg_transfer_filename_tv);

                if(tv.getText().toString().equals("")){
                    Toast.makeText(FileTransFinish.this,"抱歉，解析失败",Toast.LENGTH_SHORT).show();
                    return;
                }

                openFile(tv.getText().toString());
            }

            @Override
            public void onItemLongClick(final View parentView,final int parentPosition) {
                if(updownList.get(parentPosition) == 0){
                    //上传类型
                    return;
                }

                View popView = View.inflate(FileTransFinish.this,R.layout.dialog_file_longclick_menu,null);

                ListView lv = popView.findViewById(R.id.dialog_file_longclick_menu_lv);

                String[] menuData = {"打开","分享","删除记录","删除文件","取消"};
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FileTransFinish.this,android.R.layout.simple_list_item_1,menuData);
                lv.setAdapter(arrayAdapter);

                final PopupWindow pw = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pw.dismiss();

                        TextView tv = parentView.findViewById(R.id.fg_transfer_filename_tv);

                        if(tv.getText().toString().equals("")){
                            Toast.makeText(FileTransFinish.this,"抱歉，解析失败",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("zouguo","position:" + position);

                        switch (position){
                            case 0:
                                openFile(tv.getText().toString());
                                break;
                            case 1:
                                shareFile(tv.getText().toString());
                                break;
                            case 2:
                                //删除记录
                                LitePal.deleteAll(FileTrans.class,"filename = ? and upordown = ?",tv.getText().toString(),String.valueOf(updownList.get(parentPosition)));
                                //刷新
                                getTransDataFromDB();
                                break;
                            case 3:
                                //删除文件
                                FileTrans ft = LitePal.where("filename = ? and upordown = ?",tv.getText().toString(),String.valueOf(updownList.get(parentPosition))).findLast(FileTrans.class);

                                if(ft != null){
                                    File file = new File(ft.getFilePath());

                                    if(file.exists()){
                                        //存在
                                        if(file.delete()){
                                            Toast.makeText(FileTransFinish.this,"删除成功",Toast.LENGTH_SHORT).show();

                                            //删除记录
                                            LitePal.deleteAll(FileTrans.class,"filename = ? and upordown = ?",tv.getText().toString(),String.valueOf(updownList.get(parentPosition)));
                                            //刷新
                                            getTransDataFromDB();
                                        }else{
                                            Toast.makeText(FileTransFinish.this,"抱歉，删除本地文件失败",Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        //文件不存在，删除该条记录
                                        LitePal.deleteAll(FileTrans.class,"filename = ? and upordown = ?",tv.getText().toString(),String.valueOf(updownList.get(parentPosition)));
                                        //刷新
                                        getTransDataFromDB();
                                    }
                                }else{
                                    Toast.makeText(FileTransFinish.this,"抱歉，解析失败",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 4:
                                pw.dismiss();
                                break;
                        }
                    }
                });

                ColorDrawable cd = new ColorDrawable(0xb0000000);
                pw.setBackgroundDrawable(cd);
                pw.setOutsideTouchable(true);
                pw.setFocusable(true);
                pw.showAtLocation(file_trans_finish_parentview, Gravity.CENTER,0,0);
            }
        });

        getTransDataFromDB();
    }

    //查询全部记录
    private void getTransDataFromDB(){
        lvFileTransList.clear();
        updownList.clear();

        List<FileTrans> fileTransDBS = LitePal.findAll(FileTrans.class);
        for(FileTrans fileTrans : fileTransDBS){
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setFileName(fileTrans.getFileName());
            taskInfo.setFileLength(fileTrans.getFileLength());
            taskInfo.setFileUrl(fileTrans.getFilePath());
            taskInfo.setTranTime(fileTrans.getFileTranTime());
            taskInfo.setUpOrDownOrRemote(fileTrans.getUpOrDown());
            taskInfo.setTranGap("");
            taskInfo.setTranState("完成");

            updownList.add(fileTrans.getUpOrDown());
            lvFileTransList.add(taskInfo);
        }
        myRecTransAdapter.notifyDataSetChanged();
    }

    /**
     * 仅下载记录
     */
    private void getTransDataFromDBOnlyDown(){
        lvFileTransList.clear();
        updownList.clear();

        List<FileTrans> fileTransDBS = LitePal.where("upordown = ?","1").find(FileTrans.class);
        for(FileTrans fileTrans : fileTransDBS){
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setFileName(fileTrans.getFileName());
            taskInfo.setFileLength(fileTrans.getFileLength());
            taskInfo.setFileUrl(fileTrans.getFilePath());
            taskInfo.setTranTime(fileTrans.getFileTranTime());
            taskInfo.setUpOrDownOrRemote(fileTrans.getUpOrDown());
            taskInfo.setTranGap("");
            taskInfo.setTranState("完成");

            updownList.add(fileTrans.getUpOrDown());
            lvFileTransList.add(taskInfo);
        }
        myRecTransAdapter.notifyDataSetChanged();
    }

    /**
     * 打开下载的文件
     * @param fileName
     */
    private void openFile(String fileName){
        FileTrans ft = LitePal.where("filename = ? and upordown = ?",fileName,"1").findLast(FileTrans.class);

        if(ft != null){
            Log.d("zouguo","OpenName:" + fileName);
            Log.d("zouguo","UpOrDown:" + ft.getUpOrDown());
            Log.d("zouguo","OpenType:" + ft.getFileType());

            File file = new File(ft.getFilePath());

            if(file.exists()){
                switch(ft.getFileType()) {
                    case 0:
                        installApkUrl = fileName;

                        //apk安装
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            //8.0以上
                            boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
                            if(haveInstallPermission){
                                MyPhoneUtils.getInstance(FileTransFinish.this).installApk(ft.getFilePath());
                            }else{
                                new AlertDialog.Builder(FileTransFinish.this)
                                        .setTitle("操作提醒")
                                        .setMessage("安装应用需要打开未知来源权限，请去设置中开启权限")
                                        .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri packageUri = Uri.parse("package:"+ getPackageName());
                                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                                                startActivityForResult(intent,REQUEST_INSTALL_PERMISSION_CODE);
                                            }
                                        })
                                        .create().show();
                            }
                        }else{
                            MyPhoneUtils.getInstance(FileTransFinish.this).installApk(ft.getFilePath());
                        }
                        break;
                    case 2:
                        //xls
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/vnd.ms-excel");
                        break;
                    case 3:
                        //word
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/msword");
                        break;
                    case 4:
                        //ppt
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/vnd.ms-powerpoint");
                        break;
                    case 6:
                        //ini
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"text/plain");
                        break;
                    case 7:
                        //iso
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(),"application/*");
                        break;
                    case 9:
                        //pdf
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/pdf");
                        break;
                    case 11:
                        //txt
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"text/plain");
                        break;
                    case 12:
                        //jar包
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/java-archive");
                        break;
                    case 13:
                        //压缩包
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/*");
                        break;
                    case 14:
                        //图片
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"image/*");
                        break;
                    case 15:
                        //视频
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"video/*");
                        break;
                    case 16:
                        //种子
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"application/x-bittorrent");
                        break;
                    case 17:
                        //音频
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalResolver(ft.getFilePath(),"audio/*");
                        break;
                    default:
                        Toast.makeText(FileTransFinish.this,"抱歉，文件类型未能正确识别",Toast.LENGTH_SHORT).show();
                        break;
                }
            }else{
                //文件不存在，删除该条记录
                LitePal.deleteAll(FileTrans.class,"filename = ? and upordown = ?",fileName,"1");
                //刷新
                getTransDataFromDB();
            }
        }else{
            Toast.makeText(FileTransFinish.this,"抱歉，解析失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 分享下载的文件
     * @param fileName
     */
    private void shareFile(String fileName){
        FileTrans ft = LitePal.where("filename = ? and upordown = ?",fileName,"1").findLast(FileTrans.class);

        if(ft != null){
            Log.d("zouguo","OpenName:" + fileName);
            Log.d("zouguo","UpOrDown:" + ft.getUpOrDown());
            Log.d("zouguo","OpenType:" + ft.getFileType());

            File file = new File(ft.getFilePath());

            if(file.exists()){
                switch(ft.getFileType()) {
                    case 0:
                        //apk
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 2:
                        //xls
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 3:
                        //word
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 4:
                        //ppt
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 5:
                        //exe
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 6:
                        //ini
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "text/*");
                        break;
                    case 7:
                        //iso
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 9:
                        //pdf
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 10:
                        //psd
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 11:
                        //txt
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "text/*");
                        break;
                    case 12:
                        //jar包
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 13:
                        //压缩包
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 14:
                        //图片
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "image/*");
                        break;
                    case 15:
                        //视频
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "video/*");
                        break;
                    case 16:
                        //种子
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "application/*");
                        break;
                    case 17:
                        //音频
                        MyPhoneUtils.getInstance(FileTransFinish.this).useLocalSharer(ft.getFilePath(), "audio/*");
                        break;
                    default:
                        Toast.makeText(FileTransFinish.this, "抱歉，文件类型未能正确识别", Toast.LENGTH_SHORT).show();
                        break;
                }
            }else{
                //文件不存在，删除该条记录
                LitePal.deleteAll(FileTrans.class,"filename = ? and upordown = ?",fileName,"1");
                //刷新
                getTransDataFromDB();
            }
        }else{
            Toast.makeText(FileTransFinish.this,"抱歉，解析失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_INSTALL_PERMISSION_CODE && resultCode == RESULT_OK){
            Log.d("zouguo","OK");

            openFile(installApkUrl);
        }else{
            Log.d("zouguo","NO");
        }
    }
}
