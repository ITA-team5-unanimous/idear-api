package com.idear.backend.global.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * WebDriver 설정
 */
@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@Configuration
public class WebDriverConfig {

  private WebDriver driver;

  @Bean
  public WebDriver webDriver() {
    WebDriverManager.chromedriver().setup();

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1920,1080");
    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

    options.addArguments("--disable-images");  // 이미지 로드 비활성화
    options.addArguments("--blink-settings=imagesEnabled=false");

    this.driver = new ChromeDriver(options);

    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(90));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

    log.info("WebDriver 초기화 완료");

    return this.driver;
  }

  @PreDestroy
  public void cleanup() {
    if (driver != null) {
      driver.quit();
      log.info("WebDriver 종료");
    }
  }
}
