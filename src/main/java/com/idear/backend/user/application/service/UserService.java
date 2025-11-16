package com.idear.backend.user.application.service;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.dto.response.UserInfoResponse;
import com.idear.backend.user.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public String getNameById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if(user.getDeletedAt() != null) throw CustomException.of(ErrorCode.USER_DELETED);

        return user.getName();
    }

    @Transactional
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if(user.getDeletedAt() != null) throw CustomException.of(ErrorCode.USER_DELETED);

        return UserInfoResponse.from(user);
    }

    @Transactional
    public void updateName(
            Long userId,
            @NotBlank(message = "이름은 필수입니다.") String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if(user.getDeletedAt() != null) throw CustomException.of(ErrorCode.USER_DELETED);

        user.updateUsername(name);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if(user.getDeletedAt() != null) throw CustomException.of(ErrorCode.USER_DELETED);

        user.deleteUser();
    }
}
