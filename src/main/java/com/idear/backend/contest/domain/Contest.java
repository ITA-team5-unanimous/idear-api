package com.idear.backend.contest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "contest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Contest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("공모전 ID")
  private Long contestId;

  @Column(nullable = false, length = 255)
  @Comment("공모전 제목")
  private String title;

  @Column(length = 100)
  @Comment("주최 기관/단체명")
  private String host;

  @Column(length = 100)
  @Comment("공모전 카테고리")
  private String category;

  @Column(length = 500)
  @Comment("공모전 대표 이미지 URL")
  private String imageUrl;

  @Comment("공모전 시작일")
  private LocalDate startDate;

  @Comment("공모전 마감일")
  private LocalDate deadline;

  @Column(length = 100)
  @Comment("시상 규모")
  private String reward;

  @Column(columnDefinition = "TEXT")
  @Comment("공모전 상세 설명")
  private String description;

  @Column(length = 500, unique = true)
  @Comment("링커리어 URL")
  private String linkareerUrl;

  @Column(length = 500)
  @Comment("공식 홈페이지 URL")
  private String homepageUrl;

  @Comment("생성 시각")
  private LocalDateTime createdAt;

  @Comment("수정 시각")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /**
   * D-day 계산
   */
  public Long getDDay() {
    if (deadline == null) {
      return null;
    }
    return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
  }
}