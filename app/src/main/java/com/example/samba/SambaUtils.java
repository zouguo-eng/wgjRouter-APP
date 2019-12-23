package com.example.samba;

import android.app.NotificationManager;
import android.os.Environment;
import android.util.Log;

import com.example.bean.SmbFileInfos;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFilenameFilter;

public class SambaUtils {
    private static SambaUtils sambaUtils = null;
    private String pathJson = "";

    Gson gson = new Gson();

    public static synchronized SambaUtils getInstance(boolean newClass){
        if(newClass){
            //切换路由器时，此处生成新类
            return new SambaUtils();
        }

        if(sambaUtils == null) {
            return new SambaUtils();
        }
        return sambaUtils;
    }

    public String getPathDirFile(final String path){
        try {
//            NtlmPasswordAuthentication apa = new NtlmPasswordAuthentication(null,"root","w132836zY.");
            SmbFile smbFile = new SmbFile(path);

            if(!smbFile.exists()){
                Log.d("zouguo","地址不存在");
            }else{
                SmbFile[] files = smbFile.listFiles();
                List<SmbFileInfos> listSmbFileInfos = new ArrayList<>();

                for(SmbFile f:files){
                    if(!f.isHidden()){
                        /**
                         * 隐藏目录不支持显示
                         * 缘由：隐藏目录无法正常返回父目录
                         */
                        SmbFileInfos smbFileInfos = new SmbFileInfos();
                        smbFileInfos.setName(f.getName());
                        smbFileInfos.setDate(f.getDate());
                        smbFileInfos.setTime(f.createTime());
                        smbFileInfos.setCsize(f.getContentLength());
                        smbFileInfos.setCanRead(f.canRead());
                        smbFileInfos.setCanWrite(f.canWrite());
                        smbFileInfos.setIsFile(f.isFile());
                        smbFileInfos.setIsDir(f.isDirectory());
                        smbFileInfos.setType(f.getType());
                        smbFileInfos.setCtype(f.getContentType());
                        smbFileInfos.setParentUrl(f.getParent());
                        smbFileInfos.setCanoniUrl(f.getCanonicalPath());

                        if(f.isDirectory()){
                            /**
                             * 文件夹，统计出下层子项数目
                             * 注：此操作非常耗时
                             */
                            //smbFileInfos.setCounts(f.list().length);
                        }else{
                            /**
                             * 将long型的文件大小转为可读性
                             */
                            if(f.length() <= 1024){
                                //Byte
                                smbFileInfos.setSize(f.length());
                                smbFileInfos.setSizeUnit("B");
                            }else if(f.length() <= 1024*1024){
                                //KByte
                                smbFileInfos.setSize(f.length()/1024);
                                smbFileInfos.setSizeUnit("KB");
                            }else if(f.length() <= 1024*1024*1024){
                                //MByte
                                smbFileInfos.setSize(f.length()/1024/1024);
                                smbFileInfos.setSizeUnit("MB");
                            }else if(f.length() <= 1024*1024*1024*1024){
                                //GByte
                                smbFileInfos.setSize(f.length()/1024/1024/1024);
                                smbFileInfos.setSizeUnit("GB");
                            }else{
                                smbFileInfos.setSize(0);
                                smbFileInfos.setSizeUnit("error");
                            }
                        }

                        listSmbFileInfos.add(smbFileInfos);
                    }
                }

                pathJson = gson.toJson(listSmbFileInfos);
            }
        }catch (SmbAuthException e){
            e.printStackTrace();
            pathJson = "Error:" + e.getMessage();
        }catch (SmbException e){
            e.printStackTrace();
            pathJson = "Error:" + e.getMessage();
        }catch (MalformedURLException e){
            e.printStackTrace();
            pathJson = "Error:" + e.getMessage();
        }catch (Exception e){
            e.printStackTrace();
            pathJson = "Error:" + e.getMessage();
        }
        return pathJson;
    }

    public boolean newFolder(String currentDir,String folderName){
        boolean newFolderIsOK = false;

        try {
            SmbFile smbFileNewFolder = new SmbFile(currentDir + folderName);
            smbFileNewFolder.mkdirs();

            newFolderIsOK = true;
        }catch (SmbException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return newFolderIsOK;
    }

    public boolean deleteItem(String currentDir,String folderName){
        boolean isDelOk = false;

        try {
            SmbFile smbFileDelFolder = new SmbFile(currentDir + folderName);
            smbFileDelFolder.delete();

            isDelOk = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }

        return isDelOk;
    }

    public boolean checkFileOrDirExist(String path){
        try {
            SmbFile smbFile = new SmbFile(path);

            if(smbFile.exists()){
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 分析文件夹下的子项数目
     * @param dirPath
     * @return
     */
    public List<Integer> countDirSon(String dirPath){
        List<Integer> listSize = new ArrayList<>();
        int dirCount = 0;
        int fileCount = 0;

        try {
            SmbFile smbFile = new SmbFile(dirPath);
            SmbFile[] smblist = smbFile.listFiles();

            for(SmbFile f : smblist){
                if(f.isFile()){
                    fileCount++;
                }else{
                    dirCount++;
                }
            }

            listSize.add(smblist.length);
            listSize.add(fileCount);
            listSize.add(dirCount);
            return listSize;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (SmbException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 重命名
     * @param pathFrom
     * @param pathTo
     * @return
     */
    public boolean renameFile(String pathFrom,String pathTo){
        try {
            SmbFile smbFrom = new SmbFile(pathFrom);
            SmbFile smbTo = new SmbFile(pathTo);
            smbFrom.renameTo(smbTo);

            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return false;
    }
}
