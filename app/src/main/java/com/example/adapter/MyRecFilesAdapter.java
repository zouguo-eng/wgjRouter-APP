package com.example.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyRecFileInfos;
import com.example.event.MessageEvent;
import com.example.utils.MyDateUtils;
import com.example.utils.MyFileUtils;
import com.example.wgjrouter.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyRecFilesAdapter extends RecyclerView.Adapter {

    private Context context;
    private EventBus eventBus;
    private int routerType = 0;
    private String routerHost = "";
    private List<MyRecFileInfos> myFgLvInfosList;
    private int MODE_CHOICE = 0;//默认非选择模式
    private HashMap<Integer,Boolean> selectStateMap = new HashMap<>();;
    private List<Integer> selectList = new ArrayList<>();
    private boolean canInitData = true;

    public MyRecFilesAdapter(Context context,EventBus eventBus,int routerType, String routerHost, List<MyRecFileInfos> myFgLvInfosList){
        this.context = context;
        this.eventBus = eventBus;
        this.routerType = routerType;
        this.routerHost = routerHost;
        this.myFgLvInfosList = myFgLvInfosList;
    }

    //配置路由参数（切换路由器时）
    public void initRouterParams(int routerType, String routerHost){
        this.routerType = routerType;
        this.routerHost = routerHost;
    }

    //是否初始化选中状态列表
    public void setCanInitData(boolean canInitData){
        this.canInitData = canInitData;
    }

    //初始化选择状态
    private void initStateMap(){
        selectStateMap.clear();

        for(int i = 0;i < myFgLvInfosList.size();i++){
            selectStateMap.put(i,false);
        }
    }

    //设置单选、多选模式
    public void setMultiChoiceMode(int Mode){
        MODE_CHOICE = Mode;
        notifyDataSetChanged();
    }

    //获取单选、多选模式
    public int getMultiChoiceMode(){
        return MODE_CHOICE;
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

    //判断当前可否刷新界面,提供给  SwipeRefreshLayout
    public boolean canFresh(){
        return MODE_CHOICE == 0 ? true:false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_lv_file_infos,viewGroup,false));
        return myViewHolder;
    }

    public interface OnItemListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }

    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener){
        this.onItemListener = onItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        int[] defaultPic = {R.mipmap.isapk,R.mipmap.isdir,R.mipmap.isexcel,R.mipmap.isword,
                R.mipmap.isppt,R.mipmap.isexe,R.mipmap.isini,R.mipmap.isiso,
                R.mipmap.islink,R.mipmap.ispdf,R.mipmap.ispsd,R.mipmap.istxt,
                R.mipmap.isjar, R.mipmap.iszip,R.mipmap.ispic,R.mipmap.isvideo,
                R.mipmap.istorrent,R.mipmap.ismusic,R.mipmap.isnothing};

        final MyRecFileInfos mfli = myFgLvInfosList.get(position);

        if(MODE_CHOICE == 1){
            //多选功能
            ((MyViewHolder)viewHolder).cb_file_select.setVisibility(View.VISIBLE);
        }else{
            ((MyViewHolder)viewHolder).cb_file_select.setVisibility(View.GONE);
        }

        ((MyViewHolder)viewHolder).cb_file_select.setOnCheckedChangeListener(null);

        if(selectStateMap.get(position)){
            ((MyViewHolder)viewHolder).cb_file_select.setChecked(true);
        }else{
            ((MyViewHolder)viewHolder).cb_file_select.setChecked(false);
        }

        ((MyViewHolder)viewHolder).cb_file_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectStateMap.put(position,true);
                }else{
                    selectStateMap.put(position,false);
                }

                eventBus.post(new MessageEvent(22,getSelectItemIndex().size(),"",1,"",""));
            }
        });

        ((MyViewHolder)viewHolder).tv_file_title.setText(mfli.getFileTitle());

        boolean isFile = mfli.getIsFile();

        if(isFile){
            ((MyViewHolder)viewHolder).tv_file_size.setText(String.valueOf(mfli.getFileSize() + " " + mfli.getSizeUnit()));
        }else{
            /**
             * 文件夹--显示子项数目
             * 注：需打开显示子项开关（耗时操作）
             */
            ((MyViewHolder)viewHolder).tv_file_size.setText(String.valueOf(mfli.getItemCount()));
        }

        ((MyViewHolder)viewHolder).tv_file_date.setText(MyDateUtils.getInstance().getCurrentDate(mfli.getFileDate()));

        String httpFilePath = mfli.getFilePath();
        String httpPicUrl = "";

        switch (routerType){
            case 0:
                httpPicUrl = httpFilePath.substring((httpFilePath.indexOf("/holl/") + 5),httpFilePath.length());
                break;
            case 1:
                httpPicUrl = httpFilePath.substring((httpFilePath.indexOf("@192.168.80.1/") + 14),httpFilePath.length());
                break;
        }

        httpPicUrl = routerHost + httpPicUrl;

        String fileUrl = httpFilePath.toLowerCase();

        int label = -1;

        if(isFile){
            label = MyFileUtils.getInstance().checkFileType(fileUrl);
            if(label == 14){
                //加载网络图片
                Picasso.get().load(httpPicUrl)
                        .resize(100,100)
                        .centerInside()
                        .placeholder(R.mipmap.loading_zhanwei)
                        .error(R.mipmap.loading_faild)
                        .tag("getPhotoTag")
                        .into(((MyViewHolder)viewHolder).iv_file_img);
            }else{
                ((MyViewHolder)viewHolder).iv_file_img.setImageResource(defaultPic[label]);
            }
        }else{
            //文件夹
            ((MyViewHolder)viewHolder).iv_file_img.setImageResource(R.mipmap.isdir);
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position, @NonNull List payloads) {
        if(payloads.isEmpty()){
            if(canInitData){
                //重新加载数据时初始
                initStateMap();
                canInitData = false;
            }
            onBindViewHolder(viewHolder,position);
        }else{
            if(payloads.get(0).toString().equals("true")){
                ((MyViewHolder)viewHolder).cb_file_select.setChecked(true);
                selectStateMap.put(position,true);
            }else if(payloads.get(0).toString().equals("false")){
                ((MyViewHolder)viewHolder).cb_file_select.setChecked(false);
                selectStateMap.put(position,false);
            }

            eventBus.post(new MessageEvent(22,getSelectItemIndex().size(),"",1,"",""));
        }
    }

    @Override
    public int getItemCount() {
        return myFgLvInfosList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        CheckBox cb_file_select;
        ImageView iv_file_img;
        TextView tv_file_title;
        TextView tv_file_size;
        TextView tv_file_date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cb_file_select = itemView.findViewById(R.id.item_cb_fileselect);
            iv_file_img = itemView.findViewById(R.id.item_iv_filepic);
            tv_file_title = itemView.findViewById(R.id.item_tv_filename);
            tv_file_size = itemView.findViewById(R.id.item_tv_filesize);
            tv_file_date = itemView.findViewById(R.id.item_tv_filedate);
        }
    }
}
