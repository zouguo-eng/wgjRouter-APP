package com.example.wgjrouter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.adapter.MyFgVpFpaAdapter;
import com.example.event.MessageEvent;
import com.example.samba.MultiDownloadTask;
import com.pgyersdk.update.PgyUpdateManager;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String[] requestPermissionList = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_PERMISSION_CODE = 9736;

    ViewPager mainViewPager;
    BottomNavigationView mainBNV;
    MenuItem menuItem;

    List<Fragment> fragmentList;

    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewPager = findViewById(R.id.main_viewpager);
        mainBNV = findViewById(R.id.main_bnv);

        fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentFile());
        fragmentList.add(new FragmentTransfer());
        fragmentList.add(new FragmentHome());
        MyFgVpFpaAdapter myFgVpFpaAdapter = new MyFgVpFpaAdapter(getSupportFragmentManager(),fragmentList);
        mainViewPager.setAdapter(myFgVpFpaAdapter);

        mainBNV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_file:
                        mainViewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_transfer:
                        mainViewPager.setCurrentItem(1);
                        break;
                    case R.id.navigation_home:
                        mainViewPager.setCurrentItem(2);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if(menuItem != null){
                    menuItem.setChecked(false);
                }else{
                    menuItem = mainBNV.getMenu().getItem(0);
                    menuItem.setChecked(true);
                }
                menuItem = mainBNV.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        //申请读写存储权限 6.0之后
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            List<String> deneyPermissionList = new ArrayList<>();
            for(int i = 0;i < requestPermissionList.length;i++){
                if(checkSelfPermission(requestPermissionList[i]) != PackageManager.PERMISSION_GRANTED){
                    deneyPermissionList.add(requestPermissionList[i]);
                }
            }
            if(!deneyPermissionList.isEmpty()){
                String[]  deneyPermissionArray = deneyPermissionList.toArray(new String[deneyPermissionList.size()]);
                requestPermissions(deneyPermissionArray,REQUEST_PERMISSION_CODE);
            }
        }

        //蒲公英更新
        new PgyUpdateManager.Builder().register();

        //创建数据库
        LitePal.getDatabase();
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
                    //6.0版本之上的需要动态权限，低于6.0不会执行到此处!!
                    //判断是否点击了不再提示(是否可再重新申请)
                    boolean ii = shouldShowRequestPermissionRationale(deneyPermissions[0]);

                    //未点击>不再询问，拒绝时返回 true,表名还可再请求权限
                    Log.d("zouguo","ii1:" + String.valueOf(ii));
                    if(ii){
                        //还可再申请权限
                        new AlertDialog.Builder(MainActivity.this)
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
                                        Toast.makeText(MainActivity.this,"无此权限将无法使用上传、下载功能",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .create().show();
                    }else{
                        Toast.makeText(MainActivity.this,"读写存储器权限不足",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
            && event.getAction() == KeyEvent.ACTION_DOWN
            && event.getRepeatCount() == 0){

            if(mainViewPager.getCurrentItem() == 0){
                //获取FragmentFile中的返回按钮
                FragmentFile ff = (FragmentFile) fragmentList.get(0);
                Toolbar fg_file_toolbar = ff.getView().findViewById(R.id.fg_file_toolbar);
                View fg_file_showpic_label = ff.getView().findViewById(R.id.fg_file_showpic_label);

                if(fg_file_toolbar.getTitle() == "Holl微路由" || fg_file_toolbar.getTitle() == "智能路由器"){
                    if((System.currentTimeMillis() - exitTime) > 2000 ){
                        Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                    }else{
                        //需替换完全退出
                        finish();
                        System.exit(0);
                    }
                }else{
                    if(fg_file_showpic_label.getTag().equals("show")){
                        //当前图片查看器在显示状态，隐藏掉
                        EventBus.getDefault().postSticky(new MessageEvent(7,0,"",1,"",""));
                    }else if(fg_file_toolbar.getTitle().toString().startsWith("项:")){
                        EventBus.getDefault().postSticky(new MessageEvent(8,0,"",1,"",""));
                    }else{
                        //返回上层
                        EventBus.getDefault().postSticky(new MessageEvent(5,0,"",1,"",""));
                    }
                }
            }else{
                if((System.currentTimeMillis() - exitTime) > 2000 ){
                    Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                }else{
                    //需替换完全退出
                    finish();
                    System.exit(0);
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Picasso.get().cancelTag("getPhotoTag");

        Log.d("zouguo","MainOnDestroy");
    }
}
