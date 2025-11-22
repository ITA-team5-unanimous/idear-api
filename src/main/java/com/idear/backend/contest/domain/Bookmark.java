package com.idear.backend.contest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(
  name = "bookmark",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "contest_id"})
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Bookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("찜 ID")
  private Long bookmarkId;

  @Column(name = "user_id", nullable = false)
  @Comment("유저 ID")
  private Long userId;

  @Column(name = "contest_id", nullable = false)
  @Comment("공모전 ID")
  private Long contestId;

  @Comment("생성 시각")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public static Bookmark of(Long userId, Long contestId) {
    return Bookmark.builder()
      .userId(userId)
      .contestId(contestId)
      .build();
  }
}