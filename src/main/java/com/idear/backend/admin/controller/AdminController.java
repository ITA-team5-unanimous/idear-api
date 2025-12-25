package com.idear.backend.admin.controller;

import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.inquiry.dto.InquiryReplyRequest;
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
