package com.jsj.datacenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

@Configuration
public class XChatConfig {

    @Bean
    public Color getXChatBackgroundColor() {
        return new Color(255, 255, 255, 0); // RGBA，最后一个参数是 alpha（透明度）
        //return Color.decode("#0a2d61");
    }
}
