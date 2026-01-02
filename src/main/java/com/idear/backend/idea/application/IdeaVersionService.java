package com.idear.backend.idea.application;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.IdeaVersion;
import com.idear.backend.idea.domain.IdeaVersionTag;
import com.idear.backend.idea.dto.response.TagResponse;
import com.idear.backend.idea.infrastructure.repository.IdeaVersionRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaVersionTagRepository;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdeaVersionService {

    private final IdeaVersionRepository ideaVersionRepository;
    private final IdeaVersionTagRepository ideaVersionTagRepository;

    @Transactional
    public TagResponse addTag(User user, Long versionId, String tag) {
        IdeaVersion version = ideaVersionRepository.findById(versionId)
                .orElseThrow(() -> CustomException.of(ErrorCode.IDEA_VERSION_NOT_FOUND));

        if (!version.getIdea().getUser().getUserId().equals(user.getUserId())) {
            throw CustomException.of(ErrorCode.USER_NOT_OWNER);
        }

        IdeaVersionTag addedTag = version.addTag(tag);
        IdeaVersionTag savedTag = ideaVersionTagRepository.save(addedTag);

        return TagResponse.of(savedTag);
    }

    @Transactional
    public void deleteTag(User user, Long versionId, Long tagId) {
        IdeaVersion version = ideaVersionRepository.findById(versionId)
                .orElseThrow(() -> CustomException.of(ErrorCode.IDEA_VERSION_NOT_FOUND));

        if (!version.getIdea().getUser().getUserId().equals(user.getUserId())) {
            throw CustomException.of(ErrorCode.USER_NOT_OWNER);
        }

        boolean removed = version.removeTagById(tagId);
        if (!removed) {
            throw CustomException.of(ErrorCode.TAG_NOT_FOUND);
        }

        ideaVersionRepository.save(version);
    }
}
