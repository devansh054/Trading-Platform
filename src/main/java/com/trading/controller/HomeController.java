package com.trading.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class HomeController {
    
    @GetMapping("/test")
    public String test() {
        return "HomeController is working!";
    }
    
    // @GetMapping("/")
    // public String home() {
    //     return "redirect:/auth.html";
    // }
    
    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() throws IOException {
        Resource resource = new ClassPathResource("static/trading-dashboard.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }
}
