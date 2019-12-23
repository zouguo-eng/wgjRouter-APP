package com.example.bean;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class TaskInfo extends LitePalSupport implements Serializable {
    private int id;
    private int indexs;
    private String fileName;//文件名
    private String fileUrl;//下载地址
    private long fileLength;//文件大小
    private String dirPath;//文件夹路径
    private String tranSpeed;//速率
    private long tranFinished;//已传输大小
    private long tranTime;//耗时
    private String tranState;//传输状态:1.正在传输、2.等待传输、3.完成
    private int tranProgress;//传输进度
    private String tranGap;//仅仅是已下载、文件长度之间的分隔符
    private int upOrDownOrRemote;//标记上传、下载、远程下载
    private boolean showDirection;//是否显示传输流方向

    public TaskInfo() {

    }

    /**
     *
     * @param id
     * @param indexs
     * @param fileName
     * @param fileUrl
     * @param dirPath
     * @param tranSpeed
     * @param tranTime
     * @param tranState
     * @param tranProgress
     * @param tranFinished
     * @param tranGap
     * @param fileLength
     * @param upOrDownOrRemote
     * @param showDirection
     */
    public TaskInfo(int id, int indexs, String fileName, String fileUrl, String dirPath, String tranSpeed, long tranTime,String tranState,int tranProgress, long tranFinished,String tranGap, long fileLength, int upOrDownOrRemote, boolean showDirection) {
        this.id = id;
        this.indexs = indexs;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.dirPath = dirPath;
        this.tranSpeed = tranSpeed;
        this.tranTime = tranTime;
        this.tranState = tranState;
        this.tranProgress = tranProgress;
        this.tranFinished = tranFinished;
        this.tranGap = tranGap;
        this.fileLength = fileLength;
        this.upOrDownOrRemote = upOrDownOrRemote;
        this.showDirection = showDirection;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndexs() {
        return indexs;
    }

    public void setIndexs(int indexs) {
        this.indexs = indexs;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public long getTranTime() {
        return tranTime;
    }

    public void setTranTime(long tranTime) {
        this.tranTime = tranTime;
    }

    public int getUpOrDownOrRemote() {
        return upOrDownOrRemote;
    }

    public void setUpOrDownOrRemote(int upOrDownOrRemote) {
        this.upOrDownOrRemote = upOrDownOrRemote;
    }

    public String getTranState() {
        return tranState;
    }

    public void setTranState(String tranState) {
        this.tranState = tranState;
    }

    public String getTranSpeed() {
        return tranSpeed;
    }

    public int getTranProgress() {
        return tranProgress;
    }

    public void setTranProgress(int tranProgress) {
        this.tranProgress = tranProgress;
    }

    public void setTranSpeed(String tranSpeed) {
        this.tranSpeed = tranSpeed;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getTranGap() {
        return tranGap;
    }

    public void setTranGap(String tranGap) {
        this.tranGap = tranGap;
    }

    public long getTranFinished() {
        return tranFinished;
    }

    public void setTranFinished(long fileFinished) {
        this.tranFinished = tranFinished;
    }

    public boolean isShowDirection() {
        return showDirection;
    }

    public void setShowDirection(boolean showDirection) {
        this.showDirection = showDirection;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "id=" + id +
                ", indexs=" + indexs +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileLength=" + fileLength +
                ", dirPath='" + dirPath + '\'' +
                ", tranSpeed='" + tranSpeed + '\'' +
                ", tranFinished=" + tranFinished +
                ", tranTime=" + tranTime +
                ", upOrDownOrRemote=" + upOrDownOrRemote +
                ", showDirection=" + showDirection +
                '}';
    }
}
