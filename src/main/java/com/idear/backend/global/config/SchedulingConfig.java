package com.idear.backend.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Configuration
@EnableScheduling
public class SchedulingConfig {
  // Spring Scheduling 활성화
}