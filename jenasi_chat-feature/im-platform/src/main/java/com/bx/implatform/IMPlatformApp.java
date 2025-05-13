package com.bx.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@ComponentScan(basePackages = {"com.bx"})
@MapperScan(basePackages = {"com.bx.implatform.mapper"})
@SpringBootApplication
@EnableScheduling  // 启用定时任务功能
public class IMPlatformApp {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApp.class, args);
    }

}