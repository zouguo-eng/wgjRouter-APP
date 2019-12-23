package com.example.samba.single;

public class SambaTrans {
    private static SambaTrans sInstance = new SambaTrans();

    private ISambaTransManager iSambaTransManager = new JcifsSambaTransManager();

    public ISambaTransManager getiSambaTransManager(){
        return iSambaTransManager;
    }

    public static SambaTrans getInstance(){
        return sInstance;
    }


}
