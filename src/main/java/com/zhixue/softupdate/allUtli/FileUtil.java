package com.zhixue.softupdate.allUtli;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class FileUtil {

    @Autowired
    ServiceProperties serviceProperties;

    private  String UPLOAD_FOLDER = System.getProperty("user.dir");
    private  String PATH = "\\webapps\\file\\";
    private  String LocalUrl="E:\\imgfile\\";
    private  String ZipUrl="\\webapps\\";


    /**
     * @desc: 返回上传文件地址
     * @author: sen
     * @date: 2020/8/20 0020 10:12
     **/
    public  String getUpFileUrl(String pathName){
        int lastURL = UPLOAD_FOLDER.lastIndexOf("\\");
        String upFileUrl ="";
        if (pathName==null||pathName.equals("")){
            upFileUrl = UPLOAD_FOLDER.substring(0, lastURL) + PATH;
            //upFileUrl=LocalUrl;
        }else {
            upFileUrl = UPLOAD_FOLDER.substring(0, lastURL) + PATH+pathName+"\\";
           //upFileUrl=LocalUrl+pathName+"\\";
        }
        return upFileUrl;
    }
    public  String getZipFileUrl(){
        int lastURL = UPLOAD_FOLDER.lastIndexOf("\\");
        String zipFileUrl = UPLOAD_FOLDER.substring(0, lastURL) + ZipUrl;
        return zipFileUrl;
    }

    /**
     * @desc: 从互联网下载文件
     * @author: sen
     * @date: 2020/8/20 0020 10:15
     **/
    public  void  downLoadFromUrl(String fileType,String urlStr,String fileName,String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        File saveDir = new File(savePath);
        if(!saveDir.exists()){
            saveDir.mkdir();
        }
        File file = new File(saveDir+File.separator+fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }
        //处理文件
        //若是前端文件  前端文件进行解压操作，解压到webapps文件夹中。
        if (fileType.equals("front")){
            try {
                ZipUtil.zipUncompress(saveDir+File.separator+fileName,getZipFileUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //若是后端，则直接拷贝到文件夹中
        else if (fileType.equals("back")){
            FileChannel input = null;
            FileChannel output = null;
            try {
                input = new FileInputStream(saveDir+File.separator+fileName).getChannel();
                output = new FileOutputStream(getZipFileUrl()+"/info.war").getChannel();
                output.transferFrom(input, 0, input.size());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("info:"+url+" download success");
    }

   /**
    * @desc:  从输入流中获取字节数组
    * @author: sen
    * @date: 2020/8/20 0020 10:14
    **/
    public   byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * @desc: 获取文件的MD5
     * @author: sen
     * @date: 2020/8/20 0020 10:13
     **/
    public  String fileToBetyArray(InputStream fis) {
        MessageDigest md = null;
        //FileInputStream fis = null;
        byte[] buffer = null;
        try {
            md = MessageDigest.getInstance("MD5");
            // fis = new FileInputStream(file);
            buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                buffer.clone();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @desc: 保存json文件
     * @author: sen
     * @date: 2020/8/20 0020 10:13
     **/
    public  void setJsonToFile(String fileName,String data) {
        BufferedWriter writer = null;
        File filePath = new File(getUpFileUrl(serviceProperties.getUpdatePathName()));
        File file = new File(getUpFileUrl(serviceProperties.getUpdatePathName())+fileName+".json");
       //如果文件夹不存在
        if (!filePath.exists()){
            filePath.mkdirs();
        }
        //如果文件不存在，则新建一个
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件写入成功！");
    }

    /**
     * @desc: 获取json文件
     * @author: sen
     * @date: 2020/8/20 0020 10:13
     **/
    public  String getJsonfromFile(String fileName) {
        String Path=getUpFileUrl(serviceProperties.getUpdatePathName())+fileName+".json";
        BufferedReader reader = null;
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }



    public static String fileMd5(InputStream inputStream) {
        try {
            return String.valueOf(DigestUtils.md5Digest(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @desc: 判断返回文件类型
     * @author: sen
     * @date: 2020/7/22 0022 10:24
     **/

    public static String fileType(String type) {
        String[] imgName = {"jpg", "png", "jpeg"};
        String[] wordName = {"xls", "xlsx", "doc", "docx", "ppt", "pptx"};
        String[] videoName = {"mp4", "flv", "avi"};
        String[] musicName = {"mp3"};
        type = type.toLowerCase();
        if (Arrays.asList(imgName).contains(type)) return "1";
        else if (Arrays.asList(wordName).contains(type)) return "2";
        else if (Arrays.asList(videoName).contains(type)) return "3";
        else if (Arrays.asList(musicName).contains(type)) return "4";
        return null;
    }

    public static String fileTypePath(String type) {
        switch (type) {
            case "1":
                return "img";
            case "2":
                return "word";
            case "3":
                return "video";
            case "4":
                return "music";
            default:
                return null;
        }
    }
}
