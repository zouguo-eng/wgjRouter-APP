package com.example.utils;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MyVideoUtils {

    private static MyVideoUtils myVideoUtils;

    public static MyVideoUtils getInstance(){
        if(myVideoUtils == null){
            myVideoUtils = new MyVideoUtils();
        }
        return myVideoUtils;
    }

    public String formatVideoDuration(long duration){
        String tlength = null;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        tlength = sdf.format(duration);

        return tlength;
    }
}
