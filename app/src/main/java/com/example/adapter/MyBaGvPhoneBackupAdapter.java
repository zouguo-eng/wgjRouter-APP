package com.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyPhoneBackups;
import com.example.wgjrouter.R;

import java.util.List;

public class MyBaGvPhoneBackupAdapter extends BaseAdapter {
    Context context;
    List<MyPhoneBackups> myPhoneBackupsList;

    public MyBaGvPhoneBackupAdapter(Context context, List<MyPhoneBackups> myPhoneBackupsList){
        this.context = context;
        this.myPhoneBackupsList = myPhoneBackupsList;
    }

    @Override
    public int getCount() {
        return myPhoneBackupsList.size();
    }

    @Override
    public Object getItem(int position) {
        return myPhoneBackupsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int[] defaultPic = {R.mipmap.copy_icon_pic,R.mipmap.copy_icon_video,R.mipmap.copy_icon_music};

        if(convertView == null){
            convertView = View.inflate(context, R.layout.item_gv_phonebackup,null);
            viewHolder = new ViewHolder();
            viewHolder.item_gv_iv = convertView.findViewById(R.id.item_gv_iv);
            viewHolder.item_gv_tv_title = convertView.findViewById(R.id.item_gv_tv_title);
            viewHolder.item_gv_tv_counts = convertView.findViewById(R.id.item_gv_tv_counts);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyPhoneBackups myBaGvPhoneBackup = myPhoneBackupsList.get(position);

        switch (myBaGvPhoneBackup.getFileType()){
            case 0:
                viewHolder.item_gv_iv.setImageResource(defaultPic[0]);
                break;
            case 1:
                viewHolder.item_gv_iv.setImageResource(defaultPic[1]);
                break;
            case 2:
                viewHolder.item_gv_iv.setImageResource(defaultPic[2]);
                break;
        }

        viewHolder.item_gv_tv_title.setText(myBaGvPhoneBackup.getFileTitle());
        viewHolder.item_gv_tv_counts.setText(myBaGvPhoneBackup.getFileCounts() + "项");
        return convertView;
    }

    private class ViewHolder{
        ImageView item_gv_iv;
        TextView item_gv_tv_title;
        TextView item_gv_tv_counts;
    }
}
