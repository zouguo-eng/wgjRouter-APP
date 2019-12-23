package com.example.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyPhoneBackups;
import com.example.event.MessageEvent;
import com.example.wgjrouter.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyRecImageBackupAdapter extends RecyclerView.Adapter {
    private Context context;
    private EventBus eventBus;
    private List<MyPhoneBackups> myPhoneBackupsList;
    private Set<String> autoUpDirSet;
    private boolean canInitData = true;
    private HashMap<Integer,Boolean> selectStateMap = new HashMap<>();;
    private List<Integer> selectList = new ArrayList<>();
    private List<String> autoUpDirList = new ArrayList<>();

    public MyRecImageBackupAdapter(Context context, EventBus eventBus, List<MyPhoneBackups> myPhoneBackupsList, Set<String> autoUpDirSet){
        this.context = context;
        this.eventBus = eventBus;
        this.myPhoneBackupsList = myPhoneBackupsList;
        this.autoUpDirSet = autoUpDirSet;

        Log.d("zouguo","构造函数");
        //初始化本地上传文件夹列表
        initBackupDirList();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rec_autoback_list,viewGroup,false));
        return myViewHolder;
    }

    //是否初始化选中状态列表
    public void setCanInitData(boolean canInitData){
        this.canInitData = canInitData;
    }

    //初始化上传文件夹列表
    private void initBackupDirList(){
        autoUpDirList.clear();

        for(String str : autoUpDirSet){
            autoUpDirList.add(str);
        }
    }

    //初始化选中状态列表
    private void initStateMap(){
        selectStateMap.clear();

        for(int i = 0;i < myPhoneBackupsList.size();i++){
            //判断当前文件夹是否已开启自动上传
            if(autoUpDirList.contains(myPhoneBackupsList.get(i).getFileTitle())){
                selectStateMap.put(i,true);
            }else{
                selectStateMap.put(i,false);
            }
        }
    }

    //全选
    public void selectAll(){
        for(int i = 0;i < selectStateMap.size();i++){
            selectStateMap.put(i,true);
        }

        notifyItemRangeChanged(0,selectStateMap.size(),true);
    }

    //全不选
    public void selectNone(){
        for(int i = 0;i < selectStateMap.size();i++){
            selectStateMap.put(i,false);
        }

        notifyItemRangeChanged(0,selectStateMap.size(),false);
    }

    //判断当前是否全部选择
    public boolean isSelectAll(){
        boolean isSelectAll = true;
        for(int i = 0;i < selectStateMap.size();i++){
            if(!selectStateMap.get(i)){
                isSelectAll = false;
            }
        }
        return isSelectAll;
    }

    //返回选择的项目位置
    public List<Integer> getSelectItemIndex(){
        selectList.clear();

        for(int i = 0;i < selectStateMap.size();i++){
            if(selectStateMap.get(i)){
                selectList.add(i);
            }
        }
        return selectList;
    }

    public interface OnItemListener{
        void onItemClick(View view,int position);
    }

    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener){
        this.onItemListener = onItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        MyPhoneBackups myPhoneBackup = myPhoneBackupsList.get(position);

        ((MyViewHolder)viewHolder).item_rec_autobackup_tv_title.setText(myPhoneBackup.getFileTitle());
        ((MyViewHolder)viewHolder).item_rec_autobackup_tv_counts.setText(myPhoneBackup.getFileCounts() + "项");

        ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setOnCheckedChangeListener(null);

        if(selectStateMap.get(position)){
            ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setChecked(true);
        }else{
            ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setChecked(false);
        }

        ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectStateMap.put(position,true);
                }else{
                    selectStateMap.put(position,false);
                }

                eventBus.post(new MessageEvent(33,getSelectItemIndex().size(),"",1,"",""));
            }
        });

        String fileUrl = myPhoneBackup.getFirstItemUrl();
        int fileType = checkFileType(fileUrl);

        switch (fileType){
            case 14:
                File file = new File(fileUrl);
                Picasso.get().load(file)
                        .tag("getPhotoTag")
                        .into(((MyViewHolder)viewHolder).item_rec_autobackup_iv_pic_itempic);
                break;
            case 15:
                break;
            default:
                break;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = viewHolder.getLayoutPosition();
                onItemListener.onItemClick(viewHolder.itemView,pos);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List payloads) {
        if(payloads.isEmpty()){
            if(canInitData){
                initStateMap();
                canInitData = false;
            }
            onBindViewHolder(viewHolder,position);
        }else{
            if(payloads.get(0).toString().equals("true")){
                //选中
                ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setChecked(true);
                selectStateMap.put(position,true);
            }else if(payloads.get(0).toString().equals("false")){
                //不选中
                ((MyViewHolder)viewHolder).item_rec_autobackup_sc.setChecked(false);
                selectStateMap.put(position,false);
            }

            eventBus.post(new MessageEvent(33,getSelectItemIndex().size(),"",1,"",""));
        }
    }

    @Override
    public int getItemCount() {
        return myPhoneBackupsList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView item_rec_autobackup_iv_pic;
        ImageView item_rec_autobackup_iv_pic_itempic;
        TextView item_rec_autobackup_tv_title;
        TextView item_rec_autobackup_tv_counts;
        SwitchCompat item_rec_autobackup_sc;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_rec_autobackup_iv_pic = itemView.findViewById(R.id.item_rec_autobackup_iv_pic);
            item_rec_autobackup_iv_pic_itempic = itemView.findViewById(R.id.item_rec_autobackup_iv_pic_itempic);
            item_rec_autobackup_tv_title = itemView.findViewById(R.id.item_rec_autobackup_tv_title);
            item_rec_autobackup_tv_counts = itemView.findViewById(R.id.item_rec_autobackup_tv_counts);
            item_rec_autobackup_sc = itemView.findViewById(R.id.item_rec_autobackup_sc);
        }
    }

    private int checkFileType(String fileUrl){
        int label = 1;

        if(fileUrl.endsWith("png") || fileUrl.endsWith("jpg") || fileUrl.endsWith("bmp") || fileUrl.endsWith("gif") || fileUrl.endsWith("jpeg") || fileUrl.endsWith("ico")){
            label = 14;
        } else if(fileUrl.endsWith("apk")){
            label = 0;
        } else if(fileUrl.endsWith("xls") || fileUrl.endsWith("xlsx")){
            label = 2;
        } else if(fileUrl.endsWith("doc") || fileUrl.endsWith("docx")){
            label = 3;
        } else if(fileUrl.endsWith("ppt") || fileUrl.endsWith("pptx")){
            label = 4;
        } else if(fileUrl.endsWith("exe")){
            label = 5;
        } else if(fileUrl.endsWith("ini")){
            label = 6;
        } else if(fileUrl.endsWith("iso")){
            label = 7;
        } else if(fileUrl.endsWith("link")){
            label = 8;
        } else if(fileUrl.endsWith("pdf")){
            label = 9;
        } else if(fileUrl.endsWith("psd")){
            label = 10;
        } else if(fileUrl.endsWith("txt")){
            label = 11;
        } else if(fileUrl.endsWith("jar")){
            label = 12;
        } else if(fileUrl.endsWith("zip") || fileUrl.endsWith("rar") || fileUrl.endsWith("tar") || fileUrl.endsWith("7z")){
            label = 13;
        } else if(fileUrl.endsWith("avi") || fileUrl.endsWith("mov") || fileUrl.endsWith("mp4") || fileUrl.endsWith("mkv") || fileUrl.endsWith("wmv") || fileUrl.endsWith("flv") || fileUrl.endsWith("rmvb")){
            label = 15;
        } else if(fileUrl.endsWith("torrent")) {
            label = 16;
        }else if(fileUrl.endsWith("mp3") || fileUrl.endsWith("wma") || fileUrl.endsWith("wav")){
            label = 17;
        }else{
            label = 18;
        }

        return label;
    }
}
