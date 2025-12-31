package com.idear.backend.idea.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "idea_versions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_versions SET deleted_at = NOW() WHERE idea_version_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ideaVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id", nullable = false)
    private Idea idea;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(length = 255)
    private String shortDescription;

    @Lob
    private String description;

    @Column(length = 255)
    private String githubUrl;

    @Column(length = 255)
    private String figmaUrl;

    @OneToMany(mappedBy = "ideaVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<IdeaVersionFile> versionFiles = new HashSet<>();

    @OneToMany(mappedBy = "ideaVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<IdeaVersionImage> versionImages = new HashSet<>();

    private LocalDateTime requestedAt;

    private LocalDateTime deletedAt;

    public static IdeaVersion createInitialVersion(
            String shortDescription,
            String description,
            String githubUrl,
            String figmaUrl,
            LocalDateTime requestedAt
    ) {
        return IdeaVersion.builder()
                .versionNumber(1)
                .shortDescription(shortDescription)
                .description(description)
                .githubUrl(githubUrl)
                .figmaUrl(figmaUrl)
                .requestedAt(requestedAt)
                .build();
    }

    public static IdeaVersion createNewVersion(
            IdeaVersion previousVersion,
            Integer newVersionNumber
    ) {
        return IdeaVersion.builder()
                .versionNumber(newVersionNumber)
                .shortDescription(previousVersion.shortDescription)
                .description(previousVersion.description)
                .githubUrl(previousVersion.githubUrl)
                .figmaUrl(previousVersion.figmaUrl)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public void updateMetadataIfNeeded(String shortDescription, String description, String githubUrl, String figmaUrl) {
        this.shortDescription = shortDescription != null ? shortDescription : this.shortDescription;
        this.description = description != null ? description : this.description;
        this.githubUrl = githubUrl != null ? githubUrl : this.githubUrl;
        this.figmaUrl = figmaUrl != null ? figmaUrl : this.figmaUrl;
    }

    public void addFile(IdeaFile file) {
        IdeaVersionFile versionFile = IdeaVersionFile.of(this, file);
        versionFiles.add(versionFile);
    }

    public void addImage(IdeaImage image) {
        IdeaVersionImage versionImage = IdeaVersionImage.of(this, image);
        versionImages.add(versionImage);
    }

    public Set<IdeaFile> getFiles() {
        return versionFiles.stream()
                .map(IdeaVersionFile::getIdeaFile)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<IdeaImage> getImages() {
        return versionImages.stream()
                .map(IdeaVersionImage::getIdeaImage)
                .collect(java.util.stream.Collectors.toSet());
    }

    public void removeImagesByIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        versionImages.removeIf(vi -> imageIds.contains(vi.getIdeaImage().getIdeaImageId()));
    }

    protected void setIdea(Idea idea) {
        this.idea = idea;
    }
}
