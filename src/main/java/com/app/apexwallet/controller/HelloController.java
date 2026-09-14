package com.app.apexwallet.controller;

import com.app.apexwallet.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    public final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/api/v1/hello")
    public String hello(){
        return helloService.getMessage();
    }

}
