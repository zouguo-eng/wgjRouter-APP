package com.example.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDateUtils {
    private static MyDateUtils myDateUtils;
    private SimpleDateFormat sdf;

    public static MyDateUtils getInstance(){
        if(myDateUtils == null){
            myDateUtils = new MyDateUtils();
        }
        return myDateUtils;
    }

    public String getCurrentDate(long timestamp){
        sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    public String getSimpleTimeFromMillis(long timemills){
        String simpleTime = null;

        if(timemills < 1000){
            simpleTime = timemills + "毫秒";
        }else if(timemills >= 1000 && timemills < 1000 * 60){
            simpleTime = timemills / 1000 + "秒";
        }else if(timemills >= 1000 * 60 && timemills < 1000 * 60 * 60){
            simpleTime = timemills / 1000 / 60 + "分";
        }else if(timemills >= 1000 * 60 * 60 && timemills < 1000 * 60 * 60 * 60){
            simpleTime = timemills / 1000 / 60 / 60 + "时";
        }

        return simpleTime;
    }
}
