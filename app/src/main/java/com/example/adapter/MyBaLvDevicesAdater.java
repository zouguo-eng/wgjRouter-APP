package com.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyBaLvDevices;
import com.example.wgjrouter.R;

import java.util.List;

public class MyBaLvDevicesAdater extends BaseAdapter {
    private Context context;
    private List<MyBaLvDevices> myBaLvDevicesList;

    int[] imgSource = {R.mipmap.icon_yes,R.mipmap.icon_no};

    public MyBaLvDevicesAdater(Context context, List<MyBaLvDevices> myBaLvDevicesList){
        this.context = context;
        this.myBaLvDevicesList = myBaLvDevicesList;
    }

    @Override
    public int getCount() {
        return myBaLvDevicesList.size();
    }

    @Override
    public Object getItem(int position) {
        return myBaLvDevicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            convertView = View.inflate(context,R.layout.item_lv_devices,null);
            viewHolder = new ViewHolder();
            viewHolder.item_tv_devname = convertView.findViewById(R.id.item_tv_devname);
            viewHolder.item_tv_devip = convertView.findViewById(R.id.item_tv_devip);
            viewHolder.item_tv_devmac = convertView.findViewById(R.id.item_tv_devmac);
            viewHolder.item_iv_devaccnet = convertView.findViewById(R.id.item_iv_devaccnet);
            viewHolder.item_iv_devaccsha = convertView.findViewById(R.id.item_iv_devaccsha);
            viewHolder.item_iv_devbindmac = convertView.findViewById(R.id.item_iv_devbindmac);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyBaLvDevices myBaLvDevices = myBaLvDevicesList.get(position);
        viewHolder.item_tv_devname.setText(myBaLvDevices.getDevName());
        viewHolder.item_tv_devip.setText(myBaLvDevices.getDevIP());
        viewHolder.item_tv_devmac.setText(myBaLvDevices.getDevMac());

        if(myBaLvDevices.getDevAccNet().equals("ENABLE")){
            viewHolder.item_iv_devaccnet.setImageResource(imgSource[0]);
        }else{
            viewHolder.item_iv_devaccnet.setImageResource(imgSource[1]);
        }

        if(myBaLvDevices.getDevAccSha().equals("ENABLE")){
            viewHolder.item_iv_devaccsha.setImageResource(imgSource[0]);
        }else{
            viewHolder.item_iv_devaccsha.setImageResource(imgSource[1]);
        }

        if(myBaLvDevices.getDevBindMAC().equals("UNBIND")){
            viewHolder.item_iv_devbindmac.setImageResource(imgSource[1]);
        }else{
            viewHolder.item_iv_devbindmac.setImageResource(imgSource[0]);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView item_tv_devname;
        TextView item_tv_devip;
        TextView item_tv_devmac;
        ImageView item_iv_devaccnet;
        ImageView item_iv_devaccsha;
        ImageView item_iv_devbindmac;
    }
}
