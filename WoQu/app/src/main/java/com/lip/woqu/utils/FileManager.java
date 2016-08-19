package com.lip.woqu.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;


public class FileManager {

    public static ArrayList<String> downList = new ArrayList<String>();

    /**
     * 复制文件
     *
     * @param sourcePath
     * @param targetPath
     * @throws java.io.IOException
     */
    public static void copyFile(String sourcePath, String targetPath,
                                String folderPath) throws IOException {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return;
        }
        String temp = targetPath;
        if (temp.contains("?")) {
            temp = temp.substring(0, temp.indexOf("?"));
        }
        temp = URLDecoder.decode(temp);
        String name = temp.substring(temp.lastIndexOf("/") + 1);
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File targetFile = new File(folderPath + name);
        if (targetFile.exists()) {
            return;
        }
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    /**
     * 重命名文件
     *
     * @param sourcePath
     * @param targetPath
     * @return
     */
    public static boolean renameFile(String sourcePath, String targetPath) {
        boolean result = false;
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return result;
        }
        String temp = targetPath;
        if (temp.contains("?")) {
            temp = temp.substring(0, temp.indexOf("?"));
        }
        temp = URLDecoder.decode(temp);
        String name = temp.substring(temp.lastIndexOf("/") + 1);
        File targetFile = new File(sourceFile.getParent() + "/" + name);
        result = sourceFile.renameTo(targetFile);
        return result;
    }

    public interface RecordCallBack {
        public void recordLoadFinished(String path, String imageUrl);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     *
     * @param fileName
     *            文件名
     */
    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String result="";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                result+=tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }

    public static String readFileByLines(InputStream in) {
//        File file = new File(fileName);
        BufferedReader reader = null;
        String result="";
        try {
            InputStreamReader isr=new InputStreamReader(in);
            reader = new BufferedReader(isr);
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                result+=tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }
}
