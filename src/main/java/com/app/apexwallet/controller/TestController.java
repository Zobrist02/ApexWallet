package com.app.apexwallet.controller;

import com.app.apexwallet.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    public final HelloService helloService;

    public TestController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/api/v1/hello")
    public String hello(){
        return helloService.getMessage();
    }

    @GetMapping("/admin/test")
    public String adminTest() {
        return "You have admin access";
    }

}
