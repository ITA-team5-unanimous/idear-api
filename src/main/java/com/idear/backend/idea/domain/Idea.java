package com.idear.backend.idea.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.idear.backend.user.domain.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	//TODO 도메인 연관관계 설정
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
	private List<IdeaFile> files = new ArrayList<>();

	@Column(length = 64)
	private String proofHash;

	@Enumerated(EnumType.STRING)
	private IdeaStatus status;

	private LocalDateTime createdAt;

	public enum IdeaStatus {
		PROVING,
		COMPLETED,
		WITHDRAWN
	}

	private Idea(User user, String title, String shortDescription, String description, IdeaStatus status, LocalDateTime createdAt) {
		this.user = user;
		this.title = title;
		this.shortDescription = shortDescription;
		this.description = description;
		this.status = status;
		this.createdAt = createdAt;
	}

	public static Idea registerIdea(User user, String title, String shortDescription, String description){
		return new Idea(user, title, shortDescription, description, IdeaStatus.PROVING, LocalDateTime.now());
	}

	public void addFile(List<IdeaFile> files){
		this.files.addAll(files);
	}
}
