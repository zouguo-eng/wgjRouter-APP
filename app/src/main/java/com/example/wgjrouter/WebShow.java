package com.example.wgjrouter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class WebShow extends AppCompatActivity {

    private Toolbar help_tb;
    private ImageView help_back;
    private WebView help_wv;
    private ImageView help_rollback;

    private int REQUEST_CHOOSE_FILE_CODE = 13579;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initView();

        Intent getIntent = getIntent();
        boolean showTitle = getIntent.getBooleanExtra("web_showtitle",true);
        boolean showBack = getIntent.getBooleanExtra("web_showback",false);
        String title = getIntent.getStringExtra("web_title");
        String url = getIntent.getStringExtra("web_url");

        Log.d("zouguo",title + " - " + url);

        if(showTitle) {
            help_tb.setVisibility(View.VISIBLE);
        }else{
            help_tb.setVisibility(View.GONE);
        }

        if(showBack){
            help_back.setVisibility(View.VISIBLE);
        }else{
            help_back.setVisibility(View.GONE);
        }

        help_tb.setTitle(title);
        help_wv.loadUrl(url);
    }

    private void initView(){
        help_tb = findViewById(R.id.help_tb);
        help_back = findViewById(R.id.help_back);
        help_tb.setNavigationIcon(R.mipmap.back);
        help_rollback = findViewById(R.id.help_rollback);

        help_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        help_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        help_wv = findViewById(R.id.help_wv);

        WebSettings ws = help_wv.getSettings();
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setDefaultTextEncodingName("utf-8");
        ws.setLoadsImagesAutomatically(true);
        ws.setJavaScriptEnabled(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        //5.1以上版本默认禁止http和https的混用
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        WebViewClient wvc = new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(help_wv.canGoBack()){
                    help_rollback.setVisibility(View.VISIBLE);
                }else{
                    help_rollback.setVisibility(View.GONE);
                }
            }
        };

//        WebChromeClient wcc = new WebChromeClient(){
//            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//                Intent intent = fileChooserParams.createIntent();
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                startActivityForResult(intent,REQUEST_CHOOSE_FILE_CODE);
//                return true;
//            }
//        };

        help_wv.setWebViewClient(wvc);
//        help_wv.setWebChromeClient(wcc);

        help_rollback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help_wv.goBack();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHOOSE_FILE_CODE && resultCode == RESULT_OK){
            Log.d("zouguo","1111");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        help_wv.destroy();
    }
}
