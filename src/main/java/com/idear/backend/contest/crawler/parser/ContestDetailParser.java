package com.idear.backend.contest.crawler.parser;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
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
  public Contest parseDetailPage(String linkareerUrl) {
    try {
      log.debug("Selenium으로 상세 페이지 로드: {}", linkareerUrl);

      driver.get(linkareerUrl);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
      wait.until(ExpectedConditions.and(
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.organization-name")),
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("dl")),
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.responsive-element"))
      ));

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
        .build();

    } catch (Exception e) {
      log.error("Selenium 크롤링 실패: {}", linkareerUrl, e);
      throw CustomException.of(ErrorCode.CRAWLING_FAILED, "상세 페이지 크롤링 실패: " + linkareerUrl);
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