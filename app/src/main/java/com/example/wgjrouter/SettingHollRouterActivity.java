package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

public class SettingHollRouterActivity extends AppCompatActivity {

    private Toolbar setting_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hollrouter_setting);

        SettingHollRouterFragment settingHollRouterFragment = new SettingHollRouterFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.setting_hollrouter_fg, settingHollRouterFragment)
                .commit();

        setting_toolbar = findViewById(R.id.hollrouter_setting_toolbar);

        setting_toolbar.setTitle("汇尔路由器");
        setting_toolbar.setNavigationIcon(R.mipmap.back);
        setting_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
