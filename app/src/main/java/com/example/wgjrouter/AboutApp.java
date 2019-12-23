package com.example.wgjrouter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pgyersdk.feedback.PgyerFeedbackManager;

public class AboutApp extends AppCompatActivity {

    private Toolbar about_toolbar;
    private TextView about_tv_github;
    private ImageView about_iv_joinqq;
    private ImageView about_iv_feedback;
    private TextView about_tv_copyqq;
    private TextView about_tv_curversion;
    private ImageView about_iv_shareapp;
    private ImageView goto_wxpay;
    private ImageView goto_alipay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutapp);

        initView();
    }

    private void initView(){
        about_toolbar = findViewById(R.id.about_toolbar);
        about_tv_github = findViewById(R.id.about_tv_github);
        about_iv_joinqq = findViewById(R.id.about_iv_joinqq);
        about_iv_feedback = findViewById(R.id.about_iv_feedback);
        about_tv_copyqq = findViewById(R.id.about_tv_copyqq);
        about_tv_curversion = findViewById(R.id.about_tv_curversion);
        about_iv_shareapp = findViewById(R.id.about_iv_shareapp);
        goto_wxpay = findViewById(R.id.goto_wxpay);
        goto_alipay = findViewById(R.id.goto_alipay);

        about_tv_github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubHome = "https://github.com/zouguo-eng";

                Intent gogithub = new Intent(Intent.ACTION_VIEW);
                gogithub.setData(Uri.parse(githubHome));
                startActivity(gogithub);
            }
        });

        goto_wxpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        goto_alipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        about_toolbar.setTitle("关于");
        about_toolbar.setNavigationIcon(R.mipmap.back);
        about_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        about_iv_joinqq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!joinQQGroup("1iFbREDDfeHB_8LHkG4fc2uusd641RRy")){
                    Toast.makeText(AboutApp.this,"未安装手Q或安装的版本不支持",Toast.LENGTH_SHORT).show();
                }
            }
        });

        about_iv_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setShakeInvoke(false)       //fasle 则不触发摇一摇，最后需要调用 invoke 方法
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                        .setColorDialogTitle("#FF0000")    //设置Dialog 标题的字体颜色，默认为颜色为#ffffff
                        .setColorTitleBg("#FF0000")        //设置Dialog 标题栏的背景色，默认为颜色为#2E2D2D
                        .setBarBackgroundColor("#FF0000")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                        .setBarButtonPressedColor("#FF0000")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                        .setColorPickerBackgroundColor("#FF0000")   //设置颜色选择器的背景色,默认颜色为 #272828
                        .builder()
                        .invoke();                  //激活直接显示的方式
            }
        });

        about_tv_copyqq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //复制QQ群号到剪贴板
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd = ClipData.newPlainText("shareurl","728198774");
                cm.setPrimaryClip(cd);

                Toast.makeText(AboutApp.this,"群号已复制到剪贴板",Toast.LENGTH_SHORT).show();
            }
        });

        about_iv_shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"微管家，微路由管理器。下载地址(密码:wgj): https://www.pgyer.com/wgjly");
                startActivity(Intent.createChooser(intent,"分享到"));
            }
        });

        about_tv_curversion.setText("当前版本:" + BuildConfig.VERSION_NAME);
    }

    /****************
     *
     * 发起添加群流程。群号：微管家M1-App(728198774) 的 key 为： 1iFbREDDfeHB_8LHkG4fc2uusd641RRy
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
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
