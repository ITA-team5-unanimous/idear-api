package com.idear.backend.user.application.service;

import com.idear.backend.user.domain.User;
import com.idear.backend.user.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UserService {

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(User user) {
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void updateName(User user, String name) {
        user.updateUsername(name);
    }

    @Transactional
    public void registerPublicKey(User user, String publicKey) {
        user.registerPublicKey(publicKey);
    }

    @Transactional
    public void deleteUser(User user) {
        user.deleteUser();
    }
}
