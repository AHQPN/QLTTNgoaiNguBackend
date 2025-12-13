package org.example.qlttngoaingu.config;

import java.util.TimeZone;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TimeZoneConfig {

    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

    @PostConstruct
    public void init() {
        // Set timezone mặc định cho toàn bộ JVM
        TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE));
    }
}
