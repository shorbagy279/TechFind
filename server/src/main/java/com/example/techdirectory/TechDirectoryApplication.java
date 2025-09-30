package com.example.techdirectory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.techdirectory")
public class TechDirectoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechDirectoryApplication.class, args);
    }
}
