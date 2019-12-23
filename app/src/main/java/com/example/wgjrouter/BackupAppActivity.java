package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class BackupAppActivity extends AppCompatActivity {
    private Toolbar activity_backup_app_tb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_backup_app);

        initView();
    }

    private void initView(){
        activity_backup_app_tb = findViewById(R.id.activity_backup_app_tb);
        activity_backup_app_tb.setTitle("应用备份");
        activity_backup_app_tb.setNavigationIcon(R.mipmap.back);
        activity_backup_app_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

