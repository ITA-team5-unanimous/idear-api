package com.idear.backend.idea.domain;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ideas")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE ideas SET deleted_at = NOW() WHERE idea_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Idea {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ideaId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_id")
	private Contest contest;

	@Column(nullable = false, length = 100)
	private String title;

	@OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<IdeaVersion> versions = new ArrayList<>();

	private LocalDateTime requestedAt;

	private LocalDateTime deletedAt;

	public static Idea register(
			User user,
			Contest contest,
			String title,
			LocalDateTime requestedAt
	) {
		return Idea.builder()
				.user(user)
				.contest(contest)
				.title(title)
				.requestedAt(requestedAt)
				.build();
	}

	public void addVersion(IdeaVersion version) {
		versions.add(version);
		version.setIdea(this);
	}
}
