package com.idear.backend.certificate.application;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.idear.backend.certificate.util.ByteArrayMultipartFile;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.application.FileStorageService;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.domain.IdeaFile.RegisterStatus;
import com.idear.backend.idea.domain.IdeaVersion;
import com.idear.backend.user.domain.User;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

	private final TemplateEngine templateEngine;
	private final FileStorageService fileStorageService;

	@Value("${blockchain.contract.address}")
	private String contractAddress;

	public String generateAndUploadCertificate(IdeaFile ideaFile, User user, Idea idea) {
		if (ideaFile.getRegisterStatus() != RegisterStatus.REGISTERED) {
			throw CustomException.of(ErrorCode.FILE_NOT_REGISTERED);
		}

		try {
			String html = renderHtml(ideaFile, user, idea);
			byte[] pdf = convertToPdf(html);
			return uploadToS3(ideaFile, pdf);
		} catch (Exception e) {
			throw CustomException.of(ErrorCode.CERTIFICATE_GENERATION_FAILED);
		}
	}

	private String renderHtml(IdeaFile ideaFile, User user, Idea idea) {
		Context context = new Context();

		String ideaTitle = idea.getVersions().stream()
			.max(Comparator.comparing(IdeaVersion::getVersionNumber))
			.map(IdeaVersion::getTitle)
			.orElse(ideaFile.getOriginalFileName());

		// 로고 이미지 Base64 인코딩
		String logoBase64 = loadLogoAsBase64();

		context.setVariable("submitter", user.getName());
		context.setVariable("submissionDate", formatTimestamp(ideaFile.getRequestedTimestamp()));
		context.setVariable("ideaTitle", ideaTitle);
		context.setVariable("documentHash", ideaFile.getFileHash());
		context.setVariable("network", "Sepolia");
		context.setVariable("contractAddress", contractAddress);
		context.setVariable("commit", ideaFile.getCommit());
		context.setVariable("txHash", ideaFile.getTxHash());
		context.setVariable("blockNumber", ideaFile.getBlockNumber());
		context.setVariable("onchainTimestamp", formatTimestamp(ideaFile.getRegisteredTimestamp()));
		context.setVariable("issuedAt", LocalDateTime.now().format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		));
		context.setVariable("documentNumber", String.valueOf(ideaFile.getIdeaFileId()));
		context.setVariable("logoBase64", logoBase64);

		return templateEngine.process("certificate/certificate", context);
	}

	private String loadLogoAsBase64() {
		try (InputStream logoStream = getClass().getResourceAsStream("/templates/certificate/idea-logo.png")) {
			if (logoStream == null) {
				log.warn("Logo image not found in classpath");
				return "";
			}
			byte[] logoBytes = logoStream.readAllBytes();
			return Base64.getEncoder().encodeToString(logoBytes);
		} catch (Exception e) {
			log.warn("Failed to load logo image: {}", e.getMessage());
			return "";
		}
	}

	private byte[] convertToPdf(String html) throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.withHtmlContent(html, null);

			// 한글 폰트 로드 및 등록
			try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NanumGothic.ttf")) {
				if (fontStream != null) {
					byte[] fontBytes = fontStream.readAllBytes();
					builder.useFont(() -> new java.io.ByteArrayInputStream(fontBytes), "Nanum Gothic");
					log.info("Successfully loaded Nanum Gothic font ({} bytes)", fontBytes.length);
				} else {
					log.error("Nanum Gothic font not found in classpath at /fonts/NanumGothic.ttf");
				}
			} catch (Exception e) {
				log.error("Failed to load Nanum Gothic font", e);
			}

			builder.toStream(os);
			builder.run();
			return os.toByteArray();
		}
	}

	private String uploadToS3(IdeaFile ideaFile, byte[] pdfBytes) {
		LocalDateTime date = LocalDateTime.ofInstant(
			Instant.ofEpochMilli(ideaFile.getRegisteredTimestamp()),
			ZoneId.of("Asia/Seoul")
		);

		String uploadDir = String.format("certificates/%d/%02d",
			date.getYear(), date.getMonthValue()
		);

		String fileName = String.format("certificate_%d_%d.pdf",
			ideaFile.getIdeaFileId(), ideaFile.getRegisteredTimestamp()
		);

		ByteArrayMultipartFile pdfFile = new ByteArrayMultipartFile(
			"certificate", fileName, "application/pdf", pdfBytes
		);

		try {
			return fileStorageService.uploadFile(pdfFile, fileName, uploadDir);
		} catch (Exception e) {
			throw CustomException.of(ErrorCode.CERTIFICATE_UPLOAD_FAILED);
		}
	}

	private String formatTimestamp(Long timestamp) {
		return LocalDateTime.ofInstant(
			Instant.ofEpochMilli(timestamp),
			ZoneId.of("Asia/Seoul")
		).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
}
