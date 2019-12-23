package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

public class SettingSuperRouterActivity extends AppCompatActivity {
    private Toolbar setting_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superrouter_setting);

        SettingSuperRouterFragment ssrf = new SettingSuperRouterFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.setting_superrouter_fg,ssrf)
                .commit();

        setting_toolbar = findViewById(R.id.superrouter_setting_toolbar);

        setting_toolbar.setTitle("智能路由器");
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
