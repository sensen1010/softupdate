package com.zhixue.softupdate.allUtli;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
public class ServiceProperties {

    @Value("${nomService.fileUrl}")
    private String fileUrl;

    @Value("${nomService.serviceUrl}")
    private String service;

    @Value("${nomService.softUrl}")
    private String softUrl;

    @Value("${update.pathName}")
    private   String updatePathName;

}
