package com.zhixue.softupdate.controller;


import com.zhixue.softupdate.allUtli.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/update")
public class updateSoftController {

    @Autowired
    ServiceClient serviceClient;

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    private String update() {
        serviceClient.updateSoftService("back");
        serviceClient.updateSoftService("front");
        return "ok";
    }


}
