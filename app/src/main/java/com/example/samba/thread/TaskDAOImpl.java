package com.example.samba.thread;

import com.example.bean.TaskInfo;
import com.example.database.FileTrans;

import org.litepal.LitePal;

import java.util.List;

public class TaskDAOImpl implements TaskDAO {
    @Override
    public synchronized void insertTask(TaskInfo taskInfo) {
        taskInfo.save();
    }

    @Override
    public synchronized void insertOverTask(FileTrans fileTrans) {
        fileTrans.save();
    }

    @Override
    public List<TaskInfo> getDownloadTaskInfos() {
        return LitePal.where("upordownorremote = ? or upordownorremote = ? ","1","2").find(TaskInfo.class);
    }

    @Override
    public List<TaskInfo> getUploadTaskInfos() {
        return LitePal.where("upordownorremote = ?","0").find(TaskInfo.class);
    }

    @Override
    public synchronized void deleteTask(String url) {
        LitePal.deleteAll(TaskInfo.class,"fileurl = ?",url);
    }
}
