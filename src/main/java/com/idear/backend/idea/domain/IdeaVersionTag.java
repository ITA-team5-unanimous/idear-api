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
@Table(name = "idea_version_tags")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_version_tags SET deleted_at = NOW() WHERE idea_version_tag_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaVersionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ideaVersionTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_version_id", nullable = false)
    private IdeaVersion ideaVersion;

    @Column(nullable = false, length = 50)
    private String tag;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    private LocalDateTime deletedAt;

    public static IdeaVersionTag of(IdeaVersion ideaVersion, String tag) {
        return IdeaVersionTag.builder()
                .ideaVersion(ideaVersion)
                .tag(tag)
                .addedAt(LocalDateTime.now())
                .build();
    }
}
