package com.idear.backend.test.controller;

import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.application.service.UserService;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
