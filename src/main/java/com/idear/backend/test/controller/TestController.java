package com.idear.backend.test.controller;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<?> helloTest() {
        return ResponseEntity.ok("HELLO I-DEAR!");
    }

    @PostMapping("/throw")
    public ResponseEntity<?> throwTest() {
        throw new CustomException(ErrorCode.EXAMPLE_ERROR);
    }
}
