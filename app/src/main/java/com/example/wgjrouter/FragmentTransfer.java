package com.example.wgjrouter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ddz.floatingactionbutton.FloatingActionButton;
import com.ddz.floatingactionbutton.FloatingActionMenu;
import com.example.adapter.MyFgTransLvAdapter;
import com.example.event.MessageEvent;
import com.example.samba.MultiDownloadService;
import com.example.samba.MultiRemoteDownloadService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class FragmentTransfer extends Fragment {

    private ImageView fg_transfer_iv;
    private TabLayout fg_transfer_tl;
    private ViewPager fg_transfer_vp;
    private FloatingActionMenu fl_menu;
    private FloatingActionButton fl_menu_button_start;
    private FloatingActionButton fl_menu_button_stop;

    private List<Fragment> fragmentList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fg_transfer,container,false);

        fg_transfer_iv = view.findViewById(R.id.fg_transfer_iv);
        fg_transfer_vp = view.findViewById(R.id.fg_transfer_vp);
        fl_menu = view.findViewById(R.id.fl_menu);
        fl_menu_button_start = view.findViewById(R.id.fl_menu_button_start);
        fl_menu_button_stop = view.findViewById(R.id.fl_menu_button_stop);

        fg_transfer_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gofile = new Intent(getActivity(),FileTransFinish.class);
                startActivity(gofile);
            }
        });

        fl_menu_button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fl_menu.collapse();

                //判断当前是下载页、上传页
                switch (fg_transfer_vp.getCurrentItem()) {
                    case 0:
                        //下载界面
                        EventBus.getDefault().postSticky(new MessageEvent(21,0,"",1,"",""));
                        break;
                    case 1:
                        //上传界面
                        EventBus.getDefault().postSticky(new MessageEvent(31,0,"",0,"",""));
                        break;
                }
            }
        });

        fl_menu_button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fl_menu.collapse();

                //判断当前是下载页、上传页
                switch (fg_transfer_vp.getCurrentItem()){
                    case 0:
                        //下载界面
                        EventBus.getDefault().postSticky(new MessageEvent(22,0,"",1,"",""));
                        break;
                    case 1:
                        //上传界面
                        EventBus.getDefault().postSticky(new MessageEvent(32,0,"",0,"",""));
                        break;
                }
            }
        });


        fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentTransDownload());
        fragmentList.add(new FragmentTransUpload());
        MyFgTransLvAdapter myFgTransLvAdapter = new MyFgTransLvAdapter(getActivity().getSupportFragmentManager(),fragmentList);

        fg_transfer_vp.setAdapter(myFgTransLvAdapter);

        fg_transfer_tl = view.findViewById(R.id.fg_transfer_tl);
        fg_transfer_tl.setupWithViewPager(fg_transfer_vp);
        return view;
    }
}
