package com.idear.backend.contest.crawler.parser;

import com.idear.backend.contest.domain.Contest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 공모전 상세 페이지 파싱
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ContestDetailParser {

  private final WebDriver driver;

  /**
   * 상세 페이지 크롤링
   */
  public Contest parseDetailPage(String linkareerUrl) throws IOException {
    try {
      log.debug("Selenium으로 상세 페이지 로드: {}", linkareerUrl);

      driver.get(linkareerUrl);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

      Thread.sleep(2000);

      String pageSource = driver.getPageSource();
      Document doc = Jsoup.parse(pageSource);

      String title = getTextSafely(doc, "h1");
      String host = getTextSafely(doc, "h2.organization-name");
      String category = getTextSafely(doc, "ul.CategoryChipList__StyledWrapper-sc-756dba5c-0 li p");
      String imageUrl = getAttrSafely(doc, "img.card-image", "abs:src");
      String reward = getFieldValue(doc, "시상규모");
      String description = getDescriptionText(doc, "div.responsive-element");
      String homepageUrl = getHomepageUrl(doc);

      LocalDate[] dates = parseDateRange(doc);
      LocalDate startDate = dates[0];
      LocalDate deadline = dates[1];

      Long viewCount = parseViewCount(doc);

      return Contest.builder()
        .title(title)
        .host(host)
        .category(category)
        .imageUrl(imageUrl)
        .startDate(startDate)
        .deadline(deadline)
        .reward(reward)
        .description(description)
        .linkareerUrl(linkareerUrl)
        .homepageUrl(homepageUrl)
        .viewCount(viewCount != null ? viewCount : 0L)
        .build();

    } catch (Exception e) {
      log.error("Selenium 크롤링 실패: {}", linkareerUrl, e);
      throw new IOException("상세 페이지 크롤링 실패", e);
    }
  }

  /**
   * 상세 페이지에서 조회수만 크롤링
   */
  public Long fetchViewCount(String linkareerUrl) throws IOException {
    try {
      log.debug("Selenium으로 조회수만 로드: {}", linkareerUrl);

      driver.get(linkareerUrl);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      // 조회수 영역(or 페이지 주요 콘텐츠) 렌더 대기
      // aria-label에 '조회' 문자열이 포함된 span.count가 생길 때까지 대기, 실패 시 h1로 폴백
      try {
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("span.count[aria-label*='조회']")));
      } catch (Exception ignore) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
      }

      // 첫 렌더 직후 약간의 지연(동적 카운터 반영 여유)
      Thread.sleep(800);

      String pageSource = driver.getPageSource();
      Document doc = Jsoup.parse(pageSource);

      Long vc = parseViewCount(doc);
      log.debug("조회수 파싱 결과: {}", vc);
      return vc != null ? vc : 0L;

    } catch (Exception e) {
      log.error("조회수 크롤링 실패: {}", linkareerUrl, e);
      throw new IOException("조회수 크롤링 실패", e);
    }
  }

  /**
   * 홈페이지 URL 추출
   */
  private String getHomepageUrl(Document doc) {
    try {
      Elements dls = doc.select("dl");
      for (Element dl : dls) {
        Element dt = dl.selectFirst("dt.field-label");
        if (dt != null && dt.text().contains("홈페이지")) {
          Element dd = dl.selectFirst("dd.text");
          if (dd != null) {
            Element link = dd.selectFirst("a[href]");
            if (link != null) {
              String href = link.attr("href");
              log.debug("홈페이지 URL 발견: {}", href);
              return href;
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn("홈페이지 URL 추출 실패", e);
    }
    return null;
  }

  /**
   * 접수기간에서 시작일과 마감일 추출
   */
  private LocalDate[] parseDateRange(Document doc) {
    LocalDate startDate = null;
    LocalDate deadline = null;

    try {
      Elements dls = doc.select("dl");
      for (Element dl : dls) {
        Element dt = dl.selectFirst("dt.field-label");
        if (dt != null && dt.text().contains("접수기간")) {
          Element dd = dl.selectFirst("dd.text");
          if (dd != null) {
            Element startSpan = dd.selectFirst("span.start-at");
            if (startSpan != null) {
              Element startDateSpan = startSpan.nextElementSibling();
              if (startDateSpan != null && startDateSpan.tagName().equals("span")) {
                String dateText = startDateSpan.text().trim();
                startDate = parseDate(dateText);
                log.debug("시작일 파싱: {} → {}", dateText, startDate);
              }
            }

            Element endSpan = dd.selectFirst("span.end-at");
            if (endSpan != null) {
              Element endDateSpan = endSpan.nextElementSibling();
              if (endDateSpan != null && endDateSpan.tagName().equals("span")) {
                String dateText = endDateSpan.text().trim();
                deadline = parseDate(dateText);
                log.debug("마감일 파싱: {} → {}", dateText, deadline);
              }
            }
            break;
          }
        }
      }
    } catch (Exception e) {
      log.warn("날짜 범위 파싱 실패", e);
    }

    return new LocalDate[]{startDate, deadline};
  }

  /**
   * dt-dd 구조에서 특정 필드값 추출
   */
  private String getFieldValue(Document doc, String fieldLabel) {
    Elements dls = doc.select("dl");
    for (Element dl : dls) {
      Element dt = dl.selectFirst("dt.field-label");
      if (dt != null && dt.text().contains(fieldLabel)) {
        Element dd = dl.selectFirst("dd.text");
        return dd != null ? dd.text().trim() : null;
      }
    }
    return null;
  }

  /**
   * 조회수 파싱
   */
  private Long parseViewCount(Document doc) {
    try {
      log.debug("=== 조회수 파싱 시작 ===");

      Elements countSpans = doc.select("span.count");
      log.debug("전체 span.count 개수: {}", countSpans.size());

      for (Element countSpan : countSpans) {
        String text = countSpan.text().trim();
        String ariaLabel = countSpan.attr("aria-label");

        log.debug("span.count 발견 - aria-label: '{}', text: '{}'", ariaLabel, text);

        if (ariaLabel != null && ariaLabel.contains("조회")) {
          String numberOnly = text.replaceAll("[^0-9]", "");
          if (!numberOnly.isEmpty()) {
            Long count = Long.parseLong(numberOnly);
            log.debug("조회수 파싱 성공: {}", count);
            return count;
          }
        }
      }

      log.debug("조회수를 찾을 수 없습니다");

    } catch (Exception e) {
      log.error("조회수 파싱 실패", e);
    }

    return 0L;
  }

  /**
   * 안전한 요소 추출
   */
  private String getTextSafely(Document doc, String selector) {
    Element element = doc.selectFirst(selector);
    return element != null ? element.text().trim() : null;
  }

  private String getAttrSafely(Document doc, String selector, String attr) {
    Element element = doc.selectFirst(selector);
    return element != null ? element.attr(attr) : null;
  }

  /**
   * HTML 태그 제거하되 줄바꿈 유지
   */
  private String getDescriptionText(Document doc, String selector) {
    Element element = doc.selectFirst(selector);
    if (element != null) {
      log.debug("=== 상세설명 구조 분석 ===");

      element.select("style").remove();
      element.select("script").remove();

      StringBuilder result = new StringBuilder();

      Elements children = element.children();
      log.debug("responsive-element의 직계 자식 개수: {}", children.size());

      for (Element child : children) {
        String text = child.text().trim();
        if (!text.isEmpty()) {
          result.append(text).append("\n\n");
        }
      }

      if (result.length() == 0) {
        log.debug("직계 자식이 없음. 전체 텍스트 반환");
        return element.text().trim();
      }

      String finalText = result.toString().trim();
      log.debug("최종 텍스트 길이: {}, 줄바꿈 개수: {}",
        finalText.length(), finalText.split("\n").length - 1);

      return finalText;
    }
    return null;
  }

  private LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return null;
    }

    try {
      dateStr = dateStr.trim();

      if (dateStr.matches("\\d{4}\\.\\d{1,2}\\.\\d{1,2}")) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.M.d");
        return LocalDate.parse(dateStr, formatter);
      }

    } catch (Exception e) {
      log.warn("날짜 파싱 실패: {}", dateStr, e);
    }

    return null;
  }
}