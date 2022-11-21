package com.chenpeiyu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//开启组件扫描，扫描servlet的过滤器
@ServletComponentScan
@Slf4j
@EnableTransactionManagement
@EnableCaching
public class ReggitTakeoutApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReggitTakeoutApplication.class, args);
        log.info("项目启动成功! ");
    }
}
