package com.idear.backend.idea.application;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.domain.IdeaImage;
import com.idear.backend.idea.dto.request.FileSignatureRequest;
import com.idear.backend.idea.dto.request.IdeaCreateRequest;
import com.idear.backend.idea.dto.request.SignatureData;
import com.idear.backend.idea.dto.response.*;
import com.idear.backend.idea.infrastructure.repository.IdeaFileRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaImageRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaRepository;
import com.idear.backend.idea.util.HashUtil;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdeaService {

	private final IdeaRepository ideaRepository;
	private final IdeaFileRepository ideaFileRepository;
	private final IdeaImageRepository ideaImageRepository;
	private final FileStorageService fileStorageService;
	private final ContestRepository contestRepository;
	private final HashUtil hashUtil;

	@Transactional
	public IdeaRegistrationInitResponse initIdeaRegistration(
		User user,
		IdeaCreateRequest request,
		List<MultipartFile> images,
		List<MultipartFile> files
	) throws IOException {
		Contest contest = null;
		if (request.getContestId() != null) {
			contest = contestRepository.findById(request.getContestId())
					.orElseThrow(() -> CustomException.of(ErrorCode.CONTEST_NOT_FOUND));
		}

		LocalDateTime requestedAt = LocalDateTime.now();
		Long requestTimestamp = requestedAt.toEpochSecond(ZoneOffset.UTC);

		Idea idea = Idea.register(
				user,
				contest,
				request.getTitle(),
				request.getShortDescription(),
				request.getDescription(),
				requestedAt
		);
		idea = ideaRepository.save(idea);

		processImages(idea, images);
		List<FileHashInfo> fileHashInfoList = processFiles(idea, files, requestTimestamp);

		return IdeaRegistrationInitResponse.builder()
				.ideaId(idea.getIdeaId())
				.requestedAt(requestedAt)
				.files(fileHashInfoList)
				.build();
	}

	@Transactional
	public IdeaRegistrationCompleteResponse submitSignatures(
		User user,
		Long ideaId,
		FileSignatureRequest request
	) {
		Idea idea = ideaRepository.findById(ideaId)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.USER_NOT_OWNER);
		}

		List<FileRegistrationResult> registrationResults = new ArrayList<>();

		for (SignatureData signatureData : request.getSignatures()) {
			IdeaFile ideaFile = ideaFileRepository.findById(signatureData.getIdeaFileId())
					.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_FILE_NOT_FOUND));

			if (!ideaFile.getIdea().getIdeaId().equals(ideaId)) {
				throw CustomException.of(ErrorCode.IDEA_FILE_IDEA_MISMATCH);
			}

			String serverSignature = hashUtil.generateServerSignature(
					signatureData.getUserSignature(),
					ideaFile.getCommit(),
					ideaFile.getRequestedTimestamp()
			);

			ideaFile.submitUserSignature(signatureData.getUserSignature(), serverSignature);

			/* TODO BlockchainGatewayService 구현 및 호출
			blockchainGatewayService.requestCommitRegistration(
					ideaFile.getCommit(),
					ideaFile.getRequestedTimestamp(),
					serverSignature
			);
			*/

			registrationResults.add(FileRegistrationResult.builder()
					.ideaFileId(ideaFile.getIdeaFileId())
					.fileName(ideaFile.getOriginalFileName())
					.fileHash(ideaFile.getFileHash())
					.salt(ideaFile.getSalt())
					.commit(ideaFile.getCommit())
					.build());
		}

		return IdeaRegistrationCompleteResponse.builder()
				.ideaId(ideaId)
				.registeredFiles(registrationResults)
				.totalFiles(registrationResults.size())
				.build();
	}

	@Transactional(readOnly = true)
	public List<IdeaSummaryResponse> getIdeasByUser(User user) {
		List<Idea> ideas = ideaRepository.findAllByUser(user);

		return ideas.stream()
				.map(IdeaSummaryResponse::of)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public IdeaDetailResponse getIdeaDetail(User user, Long ideaId) {
		Idea idea = ideaRepository.findById(ideaId)
			.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.ACCESS_DENIED);
		}

		return IdeaDetailResponse.of(idea);
	}

	@Transactional
	public void deleteIdea(User user, Long ideaId) {
		Idea idea = ideaRepository.findById(ideaId)
			.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.ACCESS_DENIED);
		}

		deleteIdeaFilesFromStorage(idea.getFiles());
		deleteIdeaImagesFromStorage(idea.getImages());

		ideaFileRepository.deleteAll(idea.getFiles());
		ideaImageRepository.deleteAll(idea.getImages());
		ideaRepository.delete(idea);
	}

	private void processImages(Idea idea, List<MultipartFile> images) throws IOException {
		if (images == null || images.isEmpty()) {
			return;
		}

		for (MultipartFile image : images) {
			if (!image.isEmpty()) {
				String fileName = UUID.randomUUID().toString();
				String filePath = fileStorageService.uploadFile(image, fileName, "image");

				IdeaImage ideaImage = IdeaImage.of(
						image.getOriginalFilename(),
						fileName,
						filePath
				);

				idea.addImage(ideaImage);
			}
		}
	}

	private List<FileHashInfo> processFiles(Idea idea, List<MultipartFile> files, Long requestTimestamp) throws IOException {
		List<FileHashInfo> fileHashInfoList = new ArrayList<>();

		if (files == null || files.isEmpty()) {
			return fileHashInfoList;
		}

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				String fileName = UUID.randomUUID().toString();
				String filePath = fileStorageService.uploadFile(file, fileName, "file");

				String fileHash = hashUtil.generateFileHash(file);
				String salt = hashUtil.generateSalt();
				String commit = hashUtil.generateCommit(fileHash, salt);

				IdeaFile ideaFile = IdeaFile.initialize(
						file.getOriginalFilename(),
						fileName,
						filePath,
						fileHash,
						salt,
						commit,
						requestTimestamp
				);

				idea.addFile(ideaFile);
				ideaFile = ideaFileRepository.save(ideaFile);

				fileHashInfoList.add(FileHashInfo.builder()
						.ideaFileId(ideaFile.getIdeaFileId())
						.fileName(file.getOriginalFilename())
						.fileHash(fileHash)
						.timestamp(requestTimestamp)
						.build());
			}
		}

		return fileHashInfoList;
	}

	private void deleteIdeaFilesFromStorage(List<IdeaFile> files) {
		for (IdeaFile file : files) {
			fileStorageService.deleteFile(file.getFileName(), "file");
		}
	}

	private void deleteIdeaImagesFromStorage(List<IdeaImage> images) {
		for (IdeaImage image : images) {
			fileStorageService.deleteFile(image.getFileName(), "image");
		}
	}
}
