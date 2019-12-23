package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bean.TaskInfo;
import com.example.samba.MultiDownloadService;
import com.example.utils.MyDateUtils;
import com.example.utils.MyFileUtils;
import com.example.wgjrouter.R;

import java.util.List;

public class MyRecTransAdapter extends RecyclerView.Adapter {
    private Context context = null;
    private List<TaskInfo> taskInfoList = null;

    public MyRecTransAdapter(Context context,List<TaskInfo> taskInfoList) {
        this.context = context;
        this.taskInfoList = taskInfoList;
    }

    /**
     * 更新进度条
     * @param id
     * @param progress
     */
    public void updateProgressbar(int id,long progress){
        notifyItemChanged(id,progress);
    }

    /**
     * 获取下载队列
     * @return
     */
    public List<TaskInfo> getTransEnqueueList(){
        return taskInfoList;
    }

    /**
     * 判断任务是否在下载队列中
     * @param url
     * @return
     */
    public boolean isTaskExists(String url){
        boolean isExists = false;

        for(TaskInfo taskInfo : taskInfoList){
            if(url.equals(taskInfo.getFileUrl())){
                isExists = true;
            }
        }

        return isExists;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        MyViewHolder myViewHolder = new MyViewHolder(View.inflate(context,R.layout.item_lv_trans_infos,null));
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        int[] defaultPic = {R.mipmap.isapk,R.mipmap.isdir,R.mipmap.isexcel,R.mipmap.isword,
                R.mipmap.isppt,R.mipmap.isexe,R.mipmap.isini,R.mipmap.isiso,
                R.mipmap.islink,R.mipmap.ispdf,R.mipmap.ispsd,R.mipmap.istxt,
                R.mipmap.isjar, R.mipmap.iszip,R.mipmap.ispic,R.mipmap.isvideo,
                R.mipmap.istorrent,R.mipmap.ismusic,R.mipmap.isnothing};
        int[] transType = {R.mipmap.isupload,R.mipmap.isdownload};

        TaskInfo taskInfo = taskInfoList.get(position);
        String fileUrl = taskInfo.getFileUrl();
        String gap = taskInfo.getTranGap();
        String state = taskInfo.getTranState();

        ((MyViewHolder)viewHolder).fg_transfer_filetype_iv.setImageResource(defaultPic[MyFileUtils.getInstance().checkFileType(fileUrl)]);
        ((MyViewHolder)viewHolder).fg_transfer_filename_tv.setText(taskInfo.getFileName());
        ((MyViewHolder)viewHolder).fg_transfer_filelength_tv.setText(MyFileUtils.getInstance().formatFileSize(taskInfo.getFileLength(),0));
        ((MyViewHolder)viewHolder).fg_transfer_gap_tv.setText(gap);

        if(state.equals("完成")){
            ((MyViewHolder)viewHolder).fg_transfer_pb.setVisibility(View.GONE);
            ((MyViewHolder)viewHolder).fg_transfer_pb.setProgress(100);
        }else{
            ((MyViewHolder)viewHolder).fg_transfer_pb.setVisibility(View.VISIBLE);
            ((MyViewHolder)viewHolder).fg_transfer_pb.setProgress(taskInfo.getTranProgress());
        }

        if(gap.equals("")){
            ((MyViewHolder)viewHolder).fg_transfer_finished_tv.setText("");
            ((MyViewHolder)viewHolder).fg_transfer_time_tv.setText(MyDateUtils.getInstance().getSimpleTimeFromMillis(taskInfo.getTranTime()));
        }else{
            ((MyViewHolder)viewHolder).fg_transfer_finished_tv.setText(MyFileUtils.getInstance().formatFileSize(taskInfo.getTranFinished(),0));
        }

        if(taskInfo.isShowDirection()){
            ((MyViewHolder)viewHolder).fg_transfer_type_iv.setImageResource(transType[taskInfo.getUpOrDownOrRemote()]);
        }

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

        //暂停、开始
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position,List payloads) {
        //更新Index
        TaskInfo taskInfo = taskInfoList.get(position);
        taskInfo.setIndexs(position);

        if(payloads.isEmpty()){
            onBindViewHolder(viewHolder,position);
        }else{
            List payloadList = (List) payloads.get(0);

            if(payloadList.get(0).toString().equals("downloadProgress")){
                int progress = Integer.parseInt(payloadList.get(1).toString());
                String speed = payloadList.get(2).toString();
                long finished = Long.parseLong(payloadList.get(3).toString());
                long length = Long.parseLong(payloadList.get(4).toString());
                long usetime = Long.parseLong(payloadList.get(5).toString());

                ((MyViewHolder)viewHolder).fg_transfer_pb.setProgress(progress);
                ((MyViewHolder)viewHolder).fg_transfer_time_tv.setText(speed);
                ((MyViewHolder)viewHolder).fg_transfer_finished_tv.setText(MyFileUtils.getInstance().formatFileSize(finished,0));
                ((MyViewHolder)viewHolder).fg_transfer_gap_tv.setText("/");
                ((MyViewHolder)viewHolder).fg_transfer_filelength_tv.setText(MyFileUtils.getInstance().formatFileSize(length,0));

                taskInfo.setTranState("正在下载");
                taskInfo.setTranProgress(progress);
                taskInfo.setTranFinished(finished);
                taskInfo.setTranGap("/");
                taskInfo.setFileLength(length);
                taskInfo.setTranTime(usetime);
            }else if(payloadList.get(0).toString().equals("downloadOK")){
                int progress = Integer.parseInt(payloadList.get(1).toString());
                String speed = payloadList.get(2).toString();
                long finished = Long.parseLong(payloadList.get(3).toString());
                long length = Long.parseLong(payloadList.get(4).toString());

                Log.d("zouguo","完成:" + payloadList.toString());

                ((MyViewHolder)viewHolder).fg_transfer_pb.setProgress(progress);
                ((MyViewHolder)viewHolder).fg_transfer_time_tv.setText(speed);
                ((MyViewHolder)viewHolder).fg_transfer_finished_tv.setText("");
                ((MyViewHolder)viewHolder).fg_transfer_gap_tv.setText("");
                ((MyViewHolder)viewHolder).fg_transfer_filelength_tv.setText(MyFileUtils.getInstance().formatFileSize(length,0));

                taskInfo.setTranState("完成");
                taskInfo.setTranProgress(progress);
                taskInfo.setTranGap("");
                taskInfo.setTranFinished(finished);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskInfoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView fg_transfer_filetype_iv;
        TextView fg_transfer_filename_tv;
        ProgressBar fg_transfer_pb;
        TextView fg_transfer_finished_tv;
        TextView fg_transfer_gap_tv;
        TextView fg_transfer_filelength_tv;
        TextView fg_transfer_time_tv;
        ImageView fg_transfer_type_iv;

        public MyViewHolder(View itemView) {
            super(itemView);

            fg_transfer_filetype_iv = itemView.findViewById(R.id.fg_transfer_filetype_iv);
            fg_transfer_filename_tv = itemView.findViewById(R.id.fg_transfer_filename_tv);
            fg_transfer_pb = itemView.findViewById(R.id.fg_transfer_pb);
            fg_transfer_finished_tv = itemView.findViewById(R.id.fg_transfer_finished_tv);
            fg_transfer_gap_tv = itemView.findViewById(R.id.fg_transfer_gap_tv);
            fg_transfer_filelength_tv = itemView.findViewById(R.id.fg_transfer_filelength_tv);
            fg_transfer_time_tv = itemView.findViewById(R.id.fg_transfer_time_tv);
            fg_transfer_type_iv = itemView.findViewById(R.id.fg_transfer_type_iv);

            fg_transfer_pb.setMax(100);
        }
    }
}
