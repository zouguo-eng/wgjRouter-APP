package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

public class RouterSetWan extends AppCompatActivity {

    private Toolbar router_setwan_tb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_setwan);

        initView();
    }

    private void initView(){
        router_setwan_tb = findViewById(R.id.router_setwan_tb);
        router_setwan_tb.setNavigationIcon(R.mipmap.back);
        router_setwan_tb.setTitle("外网设置");

        router_setwan_tb.setNavigationOnClickListener(new View.OnClickListener() {
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
