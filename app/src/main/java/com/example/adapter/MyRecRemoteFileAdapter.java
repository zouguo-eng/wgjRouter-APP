package com.example.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bean.MyRecRemoteFileInfos;
import com.example.utils.MyFileUtils;
import com.example.wgjrouter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecRemoteFileAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<MyRecRemoteFileInfos> myRecRemoteFileInfos;

    public MyRecRemoteFileAdapter(Context context,List<MyRecRemoteFileInfos> myRecRemoteFileInfos){
        this.context = context;
        this.myRecRemoteFileInfos = myRecRemoteFileInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rec_remote_file_infos,viewGroup,false));
        return holder;
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

        MyRecRemoteFileInfos myRecRemoteFileInfo = myRecRemoteFileInfos.get(position);

        String title = myRecRemoteFileInfo.getTitle();
        String url = myRecRemoteFileInfo.getUrl();
        ((MyViewHolder)viewHolder).item_rec_tv.setText(title);

        //以/结尾的为文件夹
        boolean isFile = !myRecRemoteFileInfo.getTitle().endsWith("/");
        int label = -1;

        if(isFile){
            label = MyFileUtils.getInstance().checkFileType(title);
            if(label == 14){
                //加载网络图片
                //Picasso.get().setIndicatorsEnabled(true);
                Picasso.get().load(url)
                        .resize(100,100)
                        .centerInside()
                        .placeholder(R.mipmap.loading_zhanwei)
                        .error(R.mipmap.loading_faild)
                        .tag("getPhotoTag")
                        .into(((MyViewHolder)viewHolder).item_rec_iv);
            }else{
                ((MyViewHolder)viewHolder).item_rec_iv.setImageResource(defaultPic[label]);
            }
        }else{
            //文件夹
            if(title.equals("../")){
                ((MyViewHolder)viewHolder).item_rec_iv.setImageBitmap(null);
            }else{
                ((MyViewHolder)viewHolder).item_rec_iv.setImageResource(R.mipmap.isdir);
            }
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
    public int getItemCount() {
        return myRecRemoteFileInfos.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView item_rec_iv;
        TextView item_rec_tv;

        public MyViewHolder(View itemView){
            super(itemView);
            item_rec_iv = itemView.findViewById(R.id.item_rec_iv);
            item_rec_tv = itemView.findViewById(R.id.item_rec_tv);
        }
    }
}
