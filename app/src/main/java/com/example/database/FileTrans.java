package com.example.database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class FileTrans extends LitePalSupport {

    private int id;
    private int fileType;//标记文件类型
    @Column(unique = true, nullable = false)
    private String fileName;
    private long fileLength;
    private long fileTranTime;
    private String filePath;
    private String dirPath;
    private int upOrDown;//标记上传、下载
    private String fileTime;

    public FileTrans() {
    }

    public FileTrans(int id, int fileType, String fileName, long fileLength, long fileTranTime, String filePath, String dirPath, int upOrDown, String fileTime) {
        this.id = id;
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.fileTranTime = fileTranTime;
        this.filePath = filePath;
        this.dirPath = dirPath;
        this.upOrDown = upOrDown;
        this.fileTime = fileTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getFileTranTime() {
        return fileTranTime;
    }

    public void setFileTranTime(long fileTranTime) {
        this.fileTranTime = fileTranTime;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public int getUpOrDown() {
        return upOrDown;
    }

    /**
     * 0-上传
     * 1-下载
     * @param upOrDown
     */
    public void setUpOrDown(int upOrDown) {
        this.upOrDown = upOrDown;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    @Override
    public String toString() {
        return "FileTrans{" +
                "id=" + id +
                ", fileType=" + fileType +
                ", fileName='" + fileName + '\'' +
                ", fileTranTime=" + fileTranTime +
                ", filePath='" + filePath + '\'' +
                ", dirPath='" + dirPath + '\'' +
                ", upOrDown=" + upOrDown +
                ", fileTime='" + fileTime + '\'' +
                '}';
    }
}
