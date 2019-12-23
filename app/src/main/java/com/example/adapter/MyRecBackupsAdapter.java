package com.example.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyPhoneBackups;
import com.example.wgjrouter.R;

import java.util.List;

public class MyRecBackupsAdapter extends RecyclerView.Adapter {
    Context context;
    List<MyPhoneBackups> myPhoneBackupsList;

    public MyRecBackupsAdapter(Context context, List<MyPhoneBackups> myPhoneBackupsList){
        this.context = context;
        this.myPhoneBackupsList = myPhoneBackupsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rec_phonebackup,viewGroup,false));
        return viewHolder;
    }

    public interface OnItemListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }

    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener){
        this.onItemListener = onItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        int[] defaultPic = {R.mipmap.copy_icon_pic,R.mipmap.copy_icon_file,R.mipmap.copy_icon_music,
                            R.mipmap.copy_icon_wechat,R.mipmap.copy_icon_app,R.mipmap.copy_icon_massage,
                            R.mipmap.copy_icon_phone,R.mipmap.copy_icon_addressbook};

        MyPhoneBackups myPhoneBackups = myPhoneBackupsList.get(position);

        ((MyViewHolder)viewHolder).item_rec_backup_iv_typepic.setImageResource(defaultPic[myPhoneBackups.getFileType()]);
        ((MyViewHolder)viewHolder).item_rec_backup_tv_title.setText(myPhoneBackups.getFileTitle());

        if(myPhoneBackups.isAutoUpload()){
            ((MyViewHolder)viewHolder).item_rec_backup_tv_state.setText("已开启");
        }else{
            ((MyViewHolder)viewHolder).item_rec_backup_tv_state.setText("关闭");
        }

        if(onItemListener != null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    onItemListener.onItemClick(viewHolder.itemView,pos);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    onItemListener.onItemLongClick(viewHolder.itemView,pos);
                    return true;
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List payloads) {
        super.onBindViewHolder(viewHolder, position, payloads);
        if(payloads.isEmpty()){
            onBindViewHolder(viewHolder,position);
        }else{
            if(payloads.get(0).toString().equals("true")){
                ((MyViewHolder)viewHolder).item_rec_backup_tv_state.setText("已开启");
            }else{
                ((MyViewHolder)viewHolder).item_rec_backup_tv_state.setText("关闭");
            }
        }
    }

    @Override
    public int getItemCount() {
        return myPhoneBackupsList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView item_rec_backup_iv_typepic;
        TextView item_rec_backup_tv_title;
        TextView item_rec_backup_tv_state;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_rec_backup_iv_typepic = itemView.findViewById(R.id.item_rec_backup_iv_typepic);
            item_rec_backup_tv_title = itemView.findViewById(R.id.item_rec_backup_tv_title);
            item_rec_backup_tv_state = itemView.findViewById(R.id.item_rec_backup_tv_state);
        }
    }
}
