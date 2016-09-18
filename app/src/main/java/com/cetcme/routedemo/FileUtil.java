package com.cetcme.routedemo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qiuhong on 9/18/16.
 */
public class FileUtil {



    public static String FILE_PATH = Environment.getExternalStorageDirectory() + "/routes/";

    public static void saveFile(String name, String data) {

        File filePath = new File(FILE_PATH);
        if(!filePath.exists()) {
            filePath.mkdir();
        }

        try {
            FileOutputStream outStream = new FileOutputStream(new File(FILE_PATH  + name + ".txt"));
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String name) {
        FileInputStream fis = null;
        StringBuffer sBuf = new StringBuffer();
        try {
            fis = new FileInputStream(FILE_PATH + name + ".txt");
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                sBuf.append(new String(buf,0,len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null) {
                try {
                    fis.close();
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

    public static String getFileNames() {
        File f = new File(FILE_PATH);
        File[] files = f.listFiles();

        String fileNames = "";
        for (File file: files) {
            fileNames += file.getName() + "\n";
            Log.i("files", "getFileNames: \n " + file.getName() + ", " + file.length() + ", " + stampToDate(file.lastModified()));
        }
        return fileNames;
    }

    public static String getLastFileName() {
        File f = new File(FILE_PATH);
        File[] files = f.listFiles();
        return files[files.length - 1].getName();
    }

    public static void deleteFile(String fileName) {
        File file = new File(FILE_PATH, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String stampToDate(long s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        long lt = new Long(s);
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }

}
