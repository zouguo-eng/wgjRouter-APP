package com.example.event;

public class MessageEvent {

    private int eventType;
    private int fileType;
    private String fileName;
    private String eventDirPath;
    private String eventFileUrl;
    private int upOrDownOrRemote;

    public MessageEvent(int eventType,int fileType,String fileName,int upOrDownOrRemote, String eventDirPath, String eventFileUrl){
        this.eventType = eventType;
        this.fileType = fileType;
        this.fileName = fileName;
        this.upOrDownOrRemote = upOrDownOrRemote;
        this.eventDirPath = eventDirPath;
        this.eventFileUrl = eventFileUrl;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getUpOrDownOrRemote() {
        return upOrDownOrRemote;
    }

    public void setUpOrDownOrRemote(int upOrDownOrRemote) {
        this.upOrDownOrRemote = upOrDownOrRemote;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getEventDirPath() {
        return eventDirPath;
    }

    public void setEventDirPath(String eventDirPath) {
        this.eventDirPath = eventDirPath;
    }

    public String getEventFileUrl() {
        return eventFileUrl;
    }

    public void setEventFileUrl(String eventFileUrl) {
        this.eventFileUrl = eventFileUrl;
    }
}
