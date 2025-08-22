package com.example.poemai;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class PoemAiBackendApplication {
    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // 强制设置为 UTC
    }
    
    public static void main(String[] args) {
        // 添加适合Android环境的系统属性
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(PoemAiBackendApplication.class, args);
    }
}