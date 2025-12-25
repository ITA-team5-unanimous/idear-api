package com.idear.backend.admin.controller;

import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.global.security.token.TokenProvider;
import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.inquiry.dto.InquiryReplyRequest;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.infrastructure.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/admin-login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              HttpServletResponse response) {

        if ("admin@idear.com".equals(email) && "admin".equals(password)) {
            User adminUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Default admin user not found."));

            UserInfo adminInfo = new UserInfo(adminUser.getUserId(), adminUser.getName(), adminUser.getEmail(), adminUser.getProviderInfo(), adminUser.getRole());
            String accessToken = tokenProvider.generateAccessToken(adminInfo);

            Cookie cookie = new Cookie("iDear_admin_token", accessToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/api"); 
            cookie.setMaxAge(60 * 60 * 24); // 1일
            response.addCookie(cookie);

            return "redirect:/admin/inquiries";
        }

        return "redirect:/admin/login?error";
    }

    @GetMapping("/inquiries")
    public String listInquiries(Model model) {
        List<Inquiry> inquiries = inquiryService.findAllInquiries();
        model.addAttribute("inquiries", inquiries);
        return "admin/inquiries";
    }

    @GetMapping("/inquiries/{id}")
    public String inquiryDetail(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.findInquiryById(id);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("replyRequest", new InquiryReplyRequest("")); // For the form
        return "admin/inquiry-detail";
    }

    @PostMapping("/inquiries/{id}/reply")
    public String replyToInquiry(@PathVariable Long id, @ModelAttribute InquiryReplyRequest replyRequest) {
        inquiryService.replyToInquiry(id, replyRequest.responseContent());
        return "redirect:/admin/inquiries";
    }
}
