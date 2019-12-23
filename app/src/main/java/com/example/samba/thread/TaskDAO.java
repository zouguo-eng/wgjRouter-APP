package com.example.samba.thread;

import com.example.bean.TaskInfo;
import com.example.database.FileTrans;

import java.util.List;

public interface TaskDAO {
    /**
     * 添加任务
     * @param taskInfo
     */
    void insertTask(TaskInfo taskInfo);

    /**
     * 添加完成任务
     * @param fileTrans
     */
    void insertOverTask(FileTrans fileTrans);

    /**
     * 下载任务列表，本地、远程
     */
    List<TaskInfo> getDownloadTaskInfos();

    /**
     * 上传任务列表
     */
    List<TaskInfo> getUploadTaskInfos();

    /**
     * 删除任务
     * @param url
     */
    void deleteTask(String url);
}
