package org.example.library.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 接口层 / 组装根：扫描 org.example.library 全包，装配各模块（service/storage/infra）的 Bean。
 */
@SpringBootApplication(scanBasePackages = "org.example.library")
@EnableScheduling
public class LibraryWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryWebApplication.class, args);
    }
}
