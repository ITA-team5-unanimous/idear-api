package com.idear.backend.idea.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Idea {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ideaId;

	//TODO 도메인 연관관계 설정
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "member_id", nullable = false)
	// private Member member;
	//
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "contest_id")
	// private Contest contest;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 255)
	private String shortDescription;

	@Lob
	private String description;

	@OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IdeaFile> images = new ArrayList<>();

	@Column(length = 64)
	private String proofHash;

	@Enumerated(EnumType.STRING)
	private IdeaStatus status;

	private LocalDateTime createdAt;

	public enum IdeaStatus {
		REGISTERED,
		PROVING,
		COMPLETED,
		WITHDRAWN
	}
}
