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
	name = "idea_version_files",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_version_file",
		columnNames = {"idea_version_id", "idea_file_id"}
	)
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_version_files SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaVersionFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_version_id", nullable = false)
	private IdeaVersion ideaVersion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_file_id", nullable = false)
	private IdeaFile ideaFile;

	@Column(nullable = false)
	private LocalDateTime addedAt;

	private LocalDateTime deletedAt;

	public static IdeaVersionFile of(IdeaVersion ideaVersion, IdeaFile ideaFile) {
		return IdeaVersionFile.builder()
				.ideaVersion(ideaVersion)
				.ideaFile(ideaFile)
				.addedAt(LocalDateTime.now())
				.build();
	}
}
