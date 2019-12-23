package com.example.wgjrouter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class BackupAudioActivity extends AppCompatActivity {
    private Toolbar activity_backup_audio_tb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_backup_audio);

        initView();
    }

    private void initView(){
        activity_backup_audio_tb = findViewById(R.id.activity_backup_audio_tb);
        activity_backup_audio_tb.setTitle("音频备份");
        activity_backup_audio_tb.setNavigationIcon(R.mipmap.back);
        activity_backup_audio_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
