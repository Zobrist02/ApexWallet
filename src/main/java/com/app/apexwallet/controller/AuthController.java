package com.app.apexwallet.controller;

import com.app.apexwallet.dto.LoginRequest;
import com.app.apexwallet.dto.RegisterRequest;
import com.app.apexwallet.exception.UserNotFoundException;
import com.app.apexwallet.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        authService.register(request);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){
        String token = authService.login(request);

        if (token == null) {
            throw new UserNotFoundException("Invalid Email or Password");
        }

        return token;
    }
}
