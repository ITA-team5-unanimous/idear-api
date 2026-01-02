package com.idear.backend.idea.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idea_images")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_images SET deleted_at = NOW() WHERE idea_image_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ideaImageId;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String filePath;

	private LocalDateTime deletedAt;

	public static IdeaImage of(
			String originalFileName,
			String fileName,
			String filePath
	) {
		return IdeaImage.builder()
				.originalFileName(originalFileName)
				.fileName(fileName)
				.filePath(filePath)
				.build();
	}
}
