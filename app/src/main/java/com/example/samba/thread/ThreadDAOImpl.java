package com.example.samba.thread;

import android.util.Log;

import com.example.bean.ThreadInfo;

import org.litepal.LitePal;

import java.util.List;

public class ThreadDAOImpl implements ThreadDAO {
    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        threadInfo.save();
    }

    @Override
    public synchronized void deleteThread(String url) {
        LitePal.deleteAll(ThreadInfo.class,"url = ?",url);
    }

    @Override
    public synchronized void updateThread(int thread_id,String url,int finished) {
        ThreadInfo tI = LitePal.where("id = ? and url = ?",thread_id+"",url).findFirst(ThreadInfo.class);
        tI.setFinished(finished);
        tI.save();
    }

    @Override
    public List<ThreadInfo> getThreadInfos(String url) {
        return LitePal.where("url = ?",url).find(ThreadInfo.class);
    }

    @Override
    public boolean isExists(int thread_id,String url) {
        return LitePal.where("id = ? and url = ?",thread_id+"",url).find(ThreadInfo.class).isEmpty();
    }
}
