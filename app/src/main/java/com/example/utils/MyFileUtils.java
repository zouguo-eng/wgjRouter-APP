package com.example.utils;

import com.example.adapter.MyRecFilesAdapter;
import com.example.wgjrouter.R;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import io.vov.vitamio.utils.Log;

public class MyFileUtils {
    private static MyFileUtils myFileSizeFormat;

    public static MyFileUtils getInstance(){
        if(myFileSizeFormat == null){
            myFileSizeFormat = new MyFileUtils();
        }
        return myFileSizeFormat;
    }

    public String formatFileSize(long fileByteSize,int decimalPoint){
        String fFileSize = "";

        if(fileByteSize < 1024){
            fFileSize = fileByteSize + "B";
        }else if(fileByteSize >= 1024 && fileByteSize < 1024 * 1024){
            BigDecimal bigDecimal = new BigDecimal(fileByteSize / 1024);
            double bdss = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue();
            fFileSize = bdss + "KB";
        }else if(fileByteSize >= 1024 * 1024 && fileByteSize < 1024 * 1024 * 1024){
            BigDecimal bigDecimal = new BigDecimal(fileByteSize / 1024 / 1024);
            double bdss = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue();
            fFileSize = bdss + "MB";
        }else if(fileByteSize >= 1024 * 1024 * 1024 && fileByteSize < 1024 * 1024 * 1024 * 1024){
            BigDecimal bigDecimal = new BigDecimal(fileByteSize / 1024 / 1024 / 1024);
            double bdss = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue();
            fFileSize = bdss + "GB";
        }else if(fileByteSize >= 1024 * 1024 * 1024 * 1024 && fileByteSize < 1024 * 1024 * 1024 * 1024 * 024){
            BigDecimal bigDecimal = new BigDecimal(fileByteSize / 1024 / 1024 / 1024 / 1024);
            double bdss = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue();
            fFileSize = bdss + "TB";
        }

        return fFileSize;
    }

    public String formatTranSpeed(long speed,int decimalPoint){
        long bigSpeed = 0;
        String fSpeed = null;

        if(speed < 1024){
            fSpeed = speed + "B/s";
        }else if(speed >= 1024 && speed < 1024 *1024){
            bigSpeed = speed / 1024;
            BigDecimal bigDecimal = new BigDecimal(bigSpeed);
            fSpeed = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue() + "KB/s";
        }else if(speed >= 1024 * 1024 && speed < 1024 *1024 * 1024){
            bigSpeed = speed / 1024 / 1024;
            BigDecimal bigDecimal = new BigDecimal(bigSpeed);
            fSpeed = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue() + "MB/s";
        }else if(speed >= 1024 * 1024 * 1024){
            bigSpeed = speed / 1024 / 1024 / 1024;
            BigDecimal bigDecimal = new BigDecimal(bigSpeed);
            fSpeed = bigDecimal.setScale(decimalPoint,BigDecimal.ROUND_HALF_UP).doubleValue() + "GB/s";
        }

        return fSpeed;
    }

    /**
     * 根据文件Url判断文件类型
     * @param fileUrl
     * @return
     */
    public int checkFileType(String fileUrl){
        int label = 1;

        if(fileUrl.endsWith("png") || fileUrl.endsWith("jpg") || fileUrl.endsWith("bmp") || fileUrl.endsWith("gif") || fileUrl.endsWith("jpeg") || fileUrl.endsWith("ico")){
            label = 14;
        } else if(fileUrl.endsWith("apk")){
            label = 0;
        } else if(fileUrl.endsWith("xls") || fileUrl.endsWith("xlsx")){
            label = 2;
        } else if(fileUrl.endsWith("doc") || fileUrl.endsWith("docx")){
            label = 3;
        } else if(fileUrl.endsWith("ppt") || fileUrl.endsWith("pptx")){
            label = 4;
        } else if(fileUrl.endsWith("exe")){
            label = 5;
        } else if(fileUrl.endsWith("ini")){
            label = 6;
        } else if(fileUrl.endsWith("iso")){
            label = 7;
        } else if(fileUrl.endsWith("link")){
            label = 8;
        } else if(fileUrl.endsWith("pdf")){
            label = 9;
        } else if(fileUrl.endsWith("psd")){
            label = 10;
        } else if(fileUrl.endsWith("txt")){
            label = 11;
        } else if(fileUrl.endsWith("jar")){
            label = 12;
        } else if(fileUrl.endsWith("zip") || fileUrl.endsWith("rar") || fileUrl.endsWith("tar") || fileUrl.endsWith("7z")){
            label = 13;
        } else if(fileUrl.endsWith("avi") || fileUrl.endsWith("mov") || fileUrl.endsWith("mp4") || fileUrl.endsWith("mkv") || fileUrl.endsWith("wmv") || fileUrl.endsWith("flv") || fileUrl.endsWith("rmvb")){
            label = 15;
        } else if(fileUrl.endsWith("torrent")) {
            label = 16;
        }else if(fileUrl.endsWith("mp3") || fileUrl.endsWith("wma") || fileUrl.endsWith("wav")){
            label = 17;
        }else{
            label = 18;
        }

        return label;
    }
}
