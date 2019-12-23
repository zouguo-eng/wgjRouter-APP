package com.example.database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class LiveVideoList extends LitePalSupport {
    private int id;
    @Column(nullable = false)
    private String liveName;
    @Column(nullable = false)
    private String liveUrls;
    private int liveType;
    private int isPlay;
    private int isCollect;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLiveName() {
        return liveName;
    }

    public void setLiveName(String liveName) {
        this.liveName = liveName;
    }

    public String getLiveUrls() {
        return liveUrls;
    }

    public void setLiveUrls(String liveUrls) {
        this.liveUrls = liveUrls;
    }

    public int getLiveType() {
        return liveType;
    }

    public void setLiveType(int liveType) {
        this.liveType = liveType;
    }

    public int getIsPlay() {
        return isPlay;
    }

    public void setIsPlay(int isPlay) {
        this.isPlay = isPlay;
    }

    public int getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(int isCollect) {
        this.isCollect = isCollect;
    }
}
