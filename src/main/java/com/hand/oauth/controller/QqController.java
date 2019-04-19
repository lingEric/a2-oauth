package com.hand.oauth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/qq")
@RestController
public class QqController {

    @RequestMapping("/info/{qq}")
    public String info(@PathVariable("qq") String qq) {
        return "qq:"+qq+" information";
    }

    //有TEST权限的才能访问
    @PreAuthorize("hasAuthority('TEST')")
    @RequestMapping("/security")
    public String security() {
        return "hello world security";
    }

}
