package com.example.bean;

public class SmbFileInfos {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getCsize() {
        return cSize;
    }

    public void setCsize(long cSize) {
        this.cSize = cSize;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean getIsCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean getIsFile() {
        return isFile;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean getIsDir() {
        return isDir;
    }

    public void setIsDir(boolean isDir) {
        this.isDir = isDir;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCtype() {
        return cType;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public void setCtype(String cType) {
        this.cType = cType;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public String getCanoniUrl() {
        return canoniUrl;
    }

    public void setCanoniUrl(String canoniUrl) {
        this.canoniUrl = canoniUrl;
    }

    public String getSizeUnit() {
        return sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    private String name;
    private long size;
    private long date;
    private long time;
    private long cSize;
    private String sizeUnit;
    private boolean canRead;
    private boolean canWrite;
    private boolean isFile;
    private boolean isDir;
    private int type;
    private int counts;//包含子项数目
    private String cType;
    private String parentUrl;//父路径
    private String canoniUrl;//完整路径
}
