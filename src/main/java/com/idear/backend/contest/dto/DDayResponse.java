package com.idear.backend.contest.dto;

import com.idear.backend.contest.domain.Contest;

import java.time.LocalDate;

public record DDayResponse(
  String title,
  LocalDate deadline,
  Long dday
) {
  public static DDayResponse from(Contest c) {
    Long d = c.getDDay();
    return new DDayResponse(
      c.getTitle(),
      c.getDeadline(),
      d
    );
  }
}

