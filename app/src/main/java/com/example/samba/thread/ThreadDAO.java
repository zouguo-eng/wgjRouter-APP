package com.example.samba.thread;

import com.example.bean.ThreadInfo;

import java.util.List;

public interface ThreadDAO {
    /**
     * 插入线程信息
     * @param threadInfo
     */
    void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     * @param url
     */
    void deleteThread(String url);

    /**
     * 更新线程下载进度
     * @param thread_id
     * @param url
     * @param finished
     */
    void updateThread(int thread_id,String url,int finished);

    /**
     * 获取文件的线程信息
     * @param url
     * @return
     */
    List<ThreadInfo> getThreadInfos(String url);

    /**
     * 判断线程信息是否存在
     * @param thread_id
     * @param url
     * @return
     */
    boolean isExists(int thread_id,String url);
}
