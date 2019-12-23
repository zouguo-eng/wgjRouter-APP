package com.example.bean;

public class MyBaLvApInfos {

    private int apindex;
    private String apssid;
    private int apsignal;
    private String apencrypt;
    private boolean apbridged;

    public int getAPindex() {
        return apindex;
    }

    public void setAPindex(int apindex) {
        this.apindex = apindex;
    }

    public String getAPssid() {
        return apssid;
    }

    public void setAPssid(String apssid) {
        this.apssid = apssid;
    }

    public int getAPsignal() {
        return apsignal;
    }

    public void setAPsignal(int apsignal) {
        this.apsignal = apsignal;
    }

    public String getAPencrypt() {
        return apencrypt;
    }

    public void setAPencrypt(String apencrypt) {
        this.apencrypt = apencrypt;
    }

    public boolean getAPbridged() {
        return apbridged;
    }

    public void setAPbridged(boolean apbridged) {
        this.apbridged = apbridged;
    }
}
