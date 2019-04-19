package com.hand.oauth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {


    @GetMapping("/jrebel")
    @ResponseBody
    public String home() {
        return "see if jrebel is working...";
    }

    @GetMapping("/")
    public String index() {
        return "home";
    }

    //受保护资源
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/debug")
    public ResponseEntity<Object> debug() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(authentication, HttpStatus.OK);
    }

    //自定义客户端登出界面
    @GetMapping("/logout/success")
    public ResponseEntity<Object> logout() {
        return new ResponseEntity<>("user has been successfully logout!!", HttpStatus.OK);
    }

}
