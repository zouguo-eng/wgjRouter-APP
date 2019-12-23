package com.example.bean;

public class MyPhoneBackups {
    private int fileType;
    private String fileTitle;
    private int fileCounts;
    private boolean autoUpload;
    private String firstItemUrl;

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileTitle() {
        return fileTitle;
    }

    public void setFileTitle(String fileTitle) {
        this.fileTitle = fileTitle;
    }

    public int getFileCounts() {
        return fileCounts;
    }

    public void setFileCounts(int fileCounts) {
        this.fileCounts = fileCounts;
    }

    public boolean isAutoUpload() {
        return autoUpload;
    }

    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public String getFirstItemUrl() {
        return firstItemUrl;
    }

    public void setFirstItemUrl(String firstItemUrl) {
        this.firstItemUrl = firstItemUrl;
    }
}