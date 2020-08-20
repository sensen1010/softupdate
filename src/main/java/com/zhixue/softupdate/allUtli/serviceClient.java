package com.zhixue.softupdate.allUtli;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import jdk.internal.dynalink.beans.StaticClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Date;

public class serviceClient {

    @Value("${nomService.url}")
    private static String service;

    @Value("${nomService.softUrl}")
    private static String softUrl;

    public static String updateSoftService(String type) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.addIfAbsent("type", type);
        String re = HttpClient.sendPostRequest(service+softUrl, multiValueMap);
        if(re==null){
            return null;
        }else {
            JSONObject jsonObject= null;
            try {
                jsonObject =JSONObject.parseObject(re);
                if ( jsonObject.getString("code").equals("1")){
                    return null;
                }
                else {
                    JSONObject data=JSONObject.parseObject(jsonObject.getString("data"));
                    String softMd5=data.getString("softMd5");
                    String softName=data.getString("softName");
                    String softClientId=data.getString("softId");
                    String softDownUrl=data.getString("downloadUrl");
                    String softContent=data.getString("softMd5");
                    String softSize=data.getString("softSize");
                    //获取保存的json文件
                    String jsonFile=FileUtil.getJsonfromFile(type);
                    if (jsonFile!=null&&!jsonFile.equals("")){
                        JSONObject reJsonFile=JSONObject.parseObject(jsonFile);
                        String reSoftMd5=reJsonFile.getString("softMd5");
                        if (reSoftMd5.equals(softMd5)){
                            return "ISNEW";
                        }
                    }
                    //保存新文件
                    FileUtil.setJsonToFile(type,jsonObject.getString("data"));
                    //下载文件
                    String fileUrl=service+"/file/"+type+"/"+softDownUrl;
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               FileUtil.downLoadFromUrl(type,fileUrl,softDownUrl,FileUtil.getUpFileUrl("update"));
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   }).start();
                    //与数据库的MD5判断
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println(re);
        return re;
    }
}
