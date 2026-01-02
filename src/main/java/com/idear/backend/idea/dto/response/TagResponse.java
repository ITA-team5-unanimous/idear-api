package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaVersionTag;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TagResponse {
    private Long ideaVersionTagId;
    private String tag;
    private LocalDateTime addedAt;

    public static TagResponse of(IdeaVersionTag versionTag) {
        return TagResponse.builder()
                .ideaVersionTagId(versionTag.getIdeaVersionTagId())
                .tag(versionTag.getTag())
                .addedAt(versionTag.getAddedAt())
                .build();
    }
}
