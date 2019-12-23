package com.example.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyBaLvApInfos;
import com.example.wgjrouter.R;

import java.util.List;

public class MyBaLvAPInfosAdapter extends BaseAdapter {

    private Context context;
    private List<MyBaLvApInfos> myBaLvAPInfosList;

    int[] wifipic = {R.mipmap.wifi1,R.mipmap.wifi2,R.mipmap.wifi3,R.mipmap.wifi4};

    public MyBaLvAPInfosAdapter(Context context,List<MyBaLvApInfos> myBaLvAPInfosList){
        this.context = context;
        this.myBaLvAPInfosList = myBaLvAPInfosList;
    }

    @Override
    public int getCount() {
        return myBaLvAPInfosList.size();
    }

    @Override
    public Object getItem(int position) {
        return myBaLvAPInfosList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            convertView = View.inflate(context,R.layout.item_lv_apinfos,null);
            viewHolder = new ViewHolder();
            viewHolder.item_tv_apname = convertView.findViewById(R.id.item_tv_apname);
            viewHolder.item_iv_encry = convertView.findViewById(R.id.item_iv_encry);
            viewHolder.item_iv_aplevel = convertView.findViewById(R.id.item_iv_aplevel);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyBaLvApInfos myBaLvAPInfos = myBaLvAPInfosList.get(position);
        viewHolder.item_tv_apname.setText(myBaLvAPInfos.getAPssid());

        if(myBaLvAPInfos.getAPbridged()){
            //当前AP已被桥接
            viewHolder.item_tv_apname.setTextColor(Color.BLUE);
        }else{
            viewHolder.item_tv_apname.setTextColor(Color.GRAY);
        }

        if(myBaLvAPInfos.getAPencrypt().equals("")){
            //未识别的加密
            viewHolder.item_iv_encry.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.item_iv_encry.setVisibility(View.VISIBLE);
        }

        if(Math.abs(myBaLvAPInfos.getAPsignal()) > 88){
            //信号很差
            viewHolder.item_iv_aplevel.setImageResource(wifipic[0]);
        }else if(Math.abs(myBaLvAPInfos.getAPsignal()) > 77){
            viewHolder.item_iv_aplevel.setImageResource(wifipic[1]);
        }else if(Math.abs(myBaLvAPInfos.getAPsignal()) > 66){
            viewHolder.item_iv_aplevel.setImageResource(wifipic[2]);
        } else{
            viewHolder.item_iv_aplevel.setImageResource(wifipic[3]);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView item_tv_apname;
        ImageView item_iv_encry;
        ImageView item_iv_aplevel;
    }
}
