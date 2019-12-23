package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

public class SettingAppActivity extends AppCompatActivity {
    private Toolbar setting_app_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);

        SettingAppFragment spf = new SettingAppFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.selfstyle_fg,spf)
                .commit();

        initView();
    }

    private void initView(){
        setting_app_toolbar = findViewById(R.id.setting_app_toolbar);
        setting_app_toolbar.setTitle("个性化设置");
        setting_app_toolbar.setNavigationIcon(R.mipmap.back);

        setting_app_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            finish();
        }
        return false;
    }
}
