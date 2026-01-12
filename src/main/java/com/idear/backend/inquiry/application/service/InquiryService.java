package com.idear.backend.inquiry.application.service;

import com.idear.backend.email.service.EmailService;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.application.FileStorageService;
import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.inquiry.domain.InquiryImage;
import com.idear.backend.inquiry.domain.InquiryStatus;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.inquiry.dto.InquiryDetailResponse;
import com.idear.backend.inquiry.dto.InquiryResponse;
import com.idear.backend.inquiry.dto.InquiryUpdateRequest;
import com.idear.backend.inquiry.infrastructure.repository.InquiryImageRepository;
import com.idear.backend.inquiry.infrastructure.repository.InquiryRepository;
import com.idear.backend.user.domain.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryImageRepository inquiryImageRepository;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createInquiry(User user, InquiryCreateRequest request, List<MultipartFile> images) {
        if (images != null && images.size() > 4) {
            throw CustomException.of(ErrorCode.TOO_MANY_INQUIRY_IMAGES);
        }

        String title = generateTitle(request.problemDescription());
        Inquiry inquiry = Inquiry.createInquiry(
                title,
                request.occurrenceTime(),
                request.browser(),
                request.device(),
                request.problemDescription(),
                user);
        inquiryRepository.save(inquiry);

        if (images != null && !images.isEmpty()) {
            processInquiryImages(inquiry, images);
        }
    }

    private String generateTitle(String problemDescription) {
        if (problemDescription == null || problemDescription.isEmpty()) {
            return "문의";
        }

        // 첫 줄 또는 50자까지만 제목으로 사용
        String title = problemDescription.lines().findFirst().orElse(problemDescription);

        if (title.length() > 50) {
            return title.substring(0, 50) + "...";
        }

        return title;
    }

    private void processInquiryImages(Inquiry inquiry, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                validateImageFile(image);

                try {
                    String extension = getFileExtension(image.getOriginalFilename());
                    String fileName = UUID.randomUUID() + extension;

                    String imageUrl = fileStorageService.uploadFile(image, fileName, "inquiry/images");

                    InquiryImage inquiryImage = InquiryImage.createInquiryImage(inquiry, imageUrl);
                    inquiryImageRepository.save(inquiryImage);
                    inquiry.addInquiryImage(inquiryImage);
                } catch (IOException e) {
                    throw CustomException.of(ErrorCode.FILE_UPLOAD_ERROR);
                }
            }
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw CustomException.of(ErrorCode.INVALID_INQUIRY_IMAGE_FILE);
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Inquiry findInquiryById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND_INQUIRY));
    }

    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiryDetail(User user, Long id) {
        Inquiry inquiry = findInquiryById(id);

        // 사용자가 자신의 문의만 조회할 수 있도록 검증
        if (!inquiry.getUser().getUserId().equals(user.getUserId())) {
            throw CustomException.of(ErrorCode.ACCESS_DENIED);
        }

        List<String> imageUrls = inquiry.getInquiryImages().stream()
                .map(InquiryImage::getImageUrl)
                .collect(Collectors.toList());

        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getOccurrenceTime(),
                inquiry.getBrowser(),
                inquiry.getDevice(),
                inquiry.getProblemDescription(),
                inquiry.getStatus(),
                imageUrls,
                inquiry.getAnswer(),
                inquiry.getAnsweredAt(),
                inquiry.getCreatedAt());
    }

    @Transactional
    public void updateInquiry(User user, Long id, InquiryUpdateRequest request, List<MultipartFile> images) {
        if (images != null && images.size() > 4) {
            throw CustomException.of(ErrorCode.TOO_MANY_INQUIRY_IMAGES);
        }

        Inquiry inquiry = findInquiryById(id);
        if (!inquiry.getUser().getUserId().equals(user.getUserId())) {
            throw CustomException.of(ErrorCode.ACCESS_DENIED);
        }

        if (inquiry.getStatus() != InquiryStatus.RECEIVED) {
            throw CustomException.of(ErrorCode.CANNOT_UPDATE_INQUIRY);
        }

        String title = generateTitle(request.problemDescription());

        inquiry.updateInquiry(
                title,
                request.occurrenceTime(),
                request.browser(),
                request.device(),
                request.problemDescription());

        inquiry.clearImages();
        inquiryImageRepository.deleteAll(inquiry.getInquiryImages());

        if (images != null && !images.isEmpty()) {
            processInquiryImages(inquiry, images);
        }
    }

    @Transactional
    public void replyToInquiry(Long id, String responseContent) {
        Inquiry inquiry = findInquiryById(id);
        if (inquiry.getStatus() == com.idear.backend.inquiry.domain.InquiryStatus.ANSWERED) {
            throw CustomException.of(ErrorCode.ALREADY_ANSWERED);
        }

        List<String> imageUrls = inquiry.getInquiryImages().stream()
                .map(InquiryImage::getImageUrl)
                .collect(Collectors.toList());

        // Send email
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", inquiry.getUser().getName());
        variables.put("inquiryContent", inquiry.getProblemDescription());
        variables.put("responseContent", responseContent);
        variables.put("imageUrls", imageUrls);
        variables.put("hasImages", !imageUrls.isEmpty());

        emailService.sendEmailWithTemplate(
                inquiry.getUser().getEmail(),
                "[iDear] 문의주신 내용에 대한 답변입니다.",
                "inquiry-response.html",
                variables);

        inquiry.answerToInquiry(responseContent);

        inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getUserInquiries(User user) {
        List<Inquiry> inquiries = inquiryRepository.findAllByUserOrderByCreatedAtDesc(user);

        return inquiries.stream()
                .map(inquiry -> new InquiryResponse(
                        inquiry.getId(),
                        inquiry.getTitle(),
                        inquiry.getProblemDescription(),
                        inquiry.getStatus(),
                        inquiry.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
