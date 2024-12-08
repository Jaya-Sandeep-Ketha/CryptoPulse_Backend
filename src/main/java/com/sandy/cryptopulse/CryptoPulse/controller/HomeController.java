package com.sandy.cryptopulse.CryptoPulse.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public String home(){
        return "Welcome to CryptoPulse!";
    }

    @GetMapping("/api/test")
    public String secure(){
        return "Welcome to CryptoPulse Secure!";
    }
}
