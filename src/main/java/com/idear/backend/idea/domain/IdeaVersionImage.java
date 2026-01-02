package com.idear.backend.idea.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(
	name = "idea_version_images",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_version_image",
		columnNames = {"idea_version_id", "idea_image_id"}
	)
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_version_images SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaVersionImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_version_id", nullable = false)
	private IdeaVersion ideaVersion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_image_id", nullable = false)
	private IdeaImage ideaImage;

	@Column(nullable = false)
	private LocalDateTime addedAt;

	private LocalDateTime deletedAt;

	public static IdeaVersionImage of(IdeaVersion ideaVersion, IdeaImage ideaImage) {
		return IdeaVersionImage.builder()
				.ideaVersion(ideaVersion)
				.ideaImage(ideaImage)
				.addedAt(LocalDateTime.now())
				.build();
	}
}
