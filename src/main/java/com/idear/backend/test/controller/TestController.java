package com.idear.backend.test.controller;

import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;

    @GetMapping("/hello")
    public ResponseEntity<?> helloTest() {
        return ResponseEntity.ok("HELLO I-DEAR!");
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmITest(@AuthenticationPrincipal UserInfo userInfo) {
        Long userId = userInfo.id();
        String name = userService.getNameById(userId);
        return ResponseEntity.ok("YOUR NAME IS: "+name);
    }
}
