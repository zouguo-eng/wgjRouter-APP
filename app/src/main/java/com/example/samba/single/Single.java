package com.example.samba.single;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.wgjrouter.R;

import java.io.File;
import java.util.ArrayList;


public class Single extends AppCompatActivity {
    private Toolbar download_text_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single);

        initView();

        Intent getIntent = getIntent();
        ArrayList<String> queue_url = getIntent.getStringArrayListExtra("queue_url");

        Log.d("zouguo","Single:" + queue_url.toString());

        String url = "smb://root:holl0311@192.168.80.1/holl/sdb1/安全工作规程（电力监控部分）习题集.doc";

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File targetDir = new File(file.getPath() + "/com.example.wgjrouter");
        SambaTrans.getInstance().getiSambaTransManager().download(url, targetDir, new INetDownloadCallBack() {
            @Override
            public void success(File localFile,long useTime) {
                Log.d("zouguo","localFile:" + localFile.getPath() + "--耗时:" + useTime);
            }

            @Override
            public void progress(int progress) {
                Log.d("zouguo","progress:" + progress);
            }

            @Override
            public void failed(Throwable throwable) {
                Log.d("zouguo","throwable:" + throwable.toString());
            }
        });
    }

    private void initView(){
        download_text_toolbar = findViewById(R.id.download_text_toolbar);
        download_text_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
