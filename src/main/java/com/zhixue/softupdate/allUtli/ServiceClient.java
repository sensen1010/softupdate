package com.zhixue.softupdate.allUtli;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import jdk.internal.dynalink.beans.StaticClass;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Date;

@Component
public class ServiceClient {

    @Autowired
    ServiceProperties serviceProperties;
    @Autowired
    FileUtil fileUtil;

    public  String updateSoftService(String type) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.addIfAbsent("type", type.toUpperCase());
        System.out.println(serviceProperties.getService()+serviceProperties.getSoftUrl());
        String re = HttpClient.sendPostRequest(serviceProperties.getService()+serviceProperties.getSoftUrl(), multiValueMap);
        if(re==null){
            return null;
        }else {
            JSONObject jsonObject= null;
            try {
                jsonObject =JSONObject.parseObject(re);
                System.out.println(jsonObject);
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
                    String jsonFile=fileUtil.getJsonfromFile(type);
                    if (jsonFile!=null&&!jsonFile.equals("")){
                        JSONObject reJsonFile=JSONObject.parseObject(jsonFile);
                        String reSoftMd5=reJsonFile.getString("softMd5");
                        if (reSoftMd5.equals(softMd5)){
                            return "ISNEW";
                        }
                    }
                    //保存新文件
                    fileUtil.setJsonToFile(type,jsonObject.getString("data"));
                    //下载文件
                    String fileUrl=serviceProperties.getFileUrl()+type+"/"+softDownUrl;
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               fileUtil.downLoadFromUrl(type,fileUrl,softDownUrl,fileUtil.getUpFileUrl("update"));
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
