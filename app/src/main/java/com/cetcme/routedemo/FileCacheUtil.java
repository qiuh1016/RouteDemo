package com.cetcme.routedemo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by qiuhong on 9/18/16.
 */
public class FileCacheUtil {
    //定义缓存文件的名字，方便外部调用
    public static final String docCache = "locationRoute_cache.txt";//缓存文件
    //缓存超时时间
    public static final int CACHE_SHORT_TIMEOUT= 1000 * 60 * 5; // 5 分钟

    /**设置缓存
     content是要存储的内容，可以是任意格式的，不一定是字符串。
     */
    public static void setCache(String content,Context context, String cacheFileName, int mode) {
        FileOutputStream fileOutputStream = null;
        try {
            //打开文件输出流，接收参数是文件名和模式
            fileOutputStream = context.openFileOutput(cacheFileName, mode);
            fileOutputStream.write(content.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取缓存，返回字符串（JSON）
    public static String getCache(Context context, String cacheFileName) {
        FileInputStream fileInputStream = null;
        StringBuffer sBuf = new StringBuffer();
        try {
            fileInputStream = context.openFileInput(cacheFileName);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fileInputStream.read(buf)) != -1) {
                sBuf.append(new String(buf,0,len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(sBuf != null) {
            return sBuf.toString();
        }
        return null;
    }

    public static String getCachePath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static void getCacheFileNames(Context context) {
        File f = new File(getCachePath(context));
        File[] files = f.listFiles();
        Log.i("files", "getCacheFileNames: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.i("files", "getCacheFileNames: " + files[i].getName());
        }
    }

    public static void deleteFile(Context context, String cacheFileName) {
        File file = new File(context.getFilesDir(), cacheFileName);
        if (file.exists()) {
            file.delete();
        }
    }
}