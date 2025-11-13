package com.idear.backend.idea.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class IdeaFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long fileId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_id", nullable = false)
	private Idea idea;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String filePath;

	@Enumerated(EnumType.STRING)
	private FileType fileType;

	public enum FileType {
		IMAGE,
		FILE
	}

	private IdeaFile(Idea idea, String originalFileName, String fileName, String filePath, FileType fileType) {
		this.idea = idea;
		this.originalFileName = originalFileName;
		this.fileName = fileName;
		this.filePath = filePath;
		this.fileType = fileType;
	}

	public static IdeaFile registerIdeaFile(Idea idea, String originalFileName,String fileName, String filePath, FileType fileType) {
		return new IdeaFile(idea, originalFileName, fileName, filePath, fileType);
	}
}