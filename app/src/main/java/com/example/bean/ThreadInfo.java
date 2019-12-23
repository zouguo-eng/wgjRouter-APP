package com.example.bean;

import org.litepal.crud.LitePalSupport;

public class ThreadInfo extends LitePalSupport {
    private int id;
    private String url;
    private long start;
    private long ends;
    private int finished;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return ends;
    }

    public void setEnd(long ends) {
        this.ends = ends;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + ends +
                ", finished=" + finished +
                '}';
    }
}
