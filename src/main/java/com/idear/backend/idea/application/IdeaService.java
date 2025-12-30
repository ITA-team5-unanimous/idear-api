package com.idear.backend.idea.application;

import com.idear.backend.blockchain.application.BlockchainService;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.domain.IdeaImage;
import com.idear.backend.idea.domain.IdeaVersion;
import com.idear.backend.idea.dto.request.FileSignatureRequest;
import com.idear.backend.idea.dto.request.IdeaCreateRequest;
import com.idear.backend.idea.dto.request.SignatureData;
import com.idear.backend.idea.dto.response.*;
import com.idear.backend.idea.infrastructure.repository.IdeaFileRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaImageRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaVersionRepository;
import com.idear.backend.idea.util.HashUtil;
import com.idear.backend.idea.util.ServerSignatureService;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdeaService {

	private final IdeaRepository ideaRepository;
	private final IdeaVersionRepository ideaVersionRepository;
	private final IdeaFileRepository ideaFileRepository;
	private final IdeaImageRepository ideaImageRepository;
	private final FileStorageService fileStorageService;
	private final BlockchainService blockchainService;
	private final ContestRepository contestRepository;
	private final HashUtil hashUtil;
	private final ServerSignatureService serverSignatureService;

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
				requestedAt
		);
		idea = ideaRepository.save(idea);

		IdeaVersion initialVersion = IdeaVersion.createInitialVersion(
				request.getShortDescription(),
				request.getDescription(),
				requestedAt
		);
		idea.addVersion(initialVersion);
		initialVersion = ideaVersionRepository.save(initialVersion);

		processImages(initialVersion, images);
		List<FileHashInfo> fileHashInfoList = processFiles(initialVersion, files, requestTimestamp);

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

			// 현재 Idea의 모든 버전에서 이 파일이 포함되어 있는지 확인
			boolean fileOwnedByIdea = idea.getVersions().stream()
					.anyMatch(v -> v.getFiles().contains(ideaFile));
			if (!fileOwnedByIdea) {
				throw CustomException.of(ErrorCode.IDEA_FILE_IDEA_MISMATCH);
			}

			String userSignature = signatureData.getUserSignature();

			if (!userSignature.startsWith("0x")) {
				userSignature = "0x" + userSignature;
			}

			String serverSignature = serverSignatureService.generateServerSignature(
					ideaFile.getCommit(),
					ideaFile.getRequestedTimestamp(),
					userSignature
			);

			ideaFile.submitUserSignature(userSignature, serverSignature);

			blockchainService.requestCommitRegistration(
					ideaFile.getCommit(),
					ideaFile.getRequestedTimestamp(),
					userSignature,
					serverSignature
			);

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
				.map(idea -> {
					IdeaVersion latestVersion = ideaVersionRepository
							.findTopByIdeaOrderByVersionNumberDesc(idea)
							.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_VERSION_NOT_FOUND));
					return IdeaSummaryResponse.of(idea, latestVersion);
				})
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public IdeaWithVersionsResponse getIdeaVersions(User user, Long ideaId) {
		Idea idea = ideaRepository.findById(ideaId)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.ACCESS_DENIED);
		}

		List<IdeaVersion> versions = ideaVersionRepository.findAllByIdeaOrderByVersionNumberDesc(idea);

		return IdeaWithVersionsResponse.of(idea, versions);
	}

	@Transactional
	public void deleteIdea(User user, Long ideaId) {
		Idea idea = ideaRepository.findById(ideaId)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.ACCESS_DENIED);
		}

		ideaRepository.delete(idea);
	}

	private boolean hasNonEmptyFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return false;
		}
		return files.stream().anyMatch(file -> file != null && !file.isEmpty());
	}

	private void processImages(IdeaVersion ideaVersion, List<MultipartFile> images) throws IOException {
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

				ideaImage = ideaImageRepository.save(ideaImage);
				ideaVersion.addImage(ideaImage);
			}
		}
	}

	private List<FileHashInfo> processFiles(IdeaVersion ideaVersion, List<MultipartFile> files, Long requestTimestamp) throws IOException {
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

				ideaVersion.addFile(ideaFile);
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

	@Transactional
	public IdeaUpdateResponse updateIdea(
		User user,
		Long ideaId,
		List<Long> deleteFileIds,
		List<Long> deleteImageIds,
		String shortDescription,
		String description,
		List<MultipartFile> images,
		List<MultipartFile> files
	) throws IOException {
		Idea idea = ideaRepository.findById(ideaId)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		if (!idea.getUser().getUserId().equals(user.getUserId())) {
			throw CustomException.of(ErrorCode.USER_NOT_OWNER);
		}

		IdeaVersion latestVersion = ideaVersionRepository
				.findLatestByIdeaWithFilesAndImages(idea)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_VERSION_NOT_FOUND));

		boolean hasFileChanges = (deleteFileIds != null && !deleteFileIds.isEmpty())
				|| hasNonEmptyFiles(files);

		IdeaVersion targetVersion;
		List<FileHashInfo> fileHashInfoList = new ArrayList<>();
		LocalDateTime updatedAt = LocalDateTime.now();
		Long timestamp = updatedAt.toEpochSecond(ZoneOffset.UTC);

		if (hasFileChanges) {
			Integer maxVersionNumber = ideaVersionRepository
				.findMaxVersionNumberByIdea(idea)
				.orElse(0);

			IdeaVersion newVersion = IdeaVersion.createNewVersion(
				latestVersion,
				maxVersionNumber + 1
			);
			idea.addVersion(newVersion);
			newVersion = ideaVersionRepository.save(newVersion);

			// 이미지 참조 복사 (Join Entity)
			Set<IdeaImage> imagesToKeep = new HashSet<>(latestVersion.getImages());
			if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
				imagesToKeep.removeIf(img -> deleteImageIds.contains(img.getIdeaImageId()));
			}
			for (IdeaImage image : imagesToKeep) {
				newVersion.addImage(image);
			}

			// 파일 참조 복사 (Join Entity)
			Set<IdeaFile> filesToKeep = new HashSet<>(latestVersion.getFiles());
			if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
				filesToKeep.removeIf(file -> deleteFileIds.contains(file.getIdeaFileId()));
			}
			for (IdeaFile file : filesToKeep) {
				newVersion.addFile(file);
			}

			fileHashInfoList = processFiles(newVersion, files, timestamp);

			if (shortDescription != null || description != null) {
				newVersion.updateMetadata(
					shortDescription != null ? shortDescription : newVersion.getShortDescription(),
					description != null ? description : newVersion.getDescription()
				);
			}

			targetVersion = newVersion;
		} else {
			if (shortDescription != null || description != null) {
				latestVersion.updateMetadata(
					shortDescription != null ? shortDescription : latestVersion.getShortDescription(),
					description != null ? description : latestVersion.getDescription()
				);
			}

			targetVersion = latestVersion;
		}

		// 메타만 변경 시 이미지 관계 제거
		if (!hasFileChanges && deleteImageIds != null && !deleteImageIds.isEmpty()) {
			targetVersion.removeImagesByIds(deleteImageIds);
		}

		processImages(targetVersion, images);

		return IdeaUpdateResponse.builder()
			.ideaId(ideaId)
			.versionNumber(targetVersion.getVersionNumber())
			.updatedAt(updatedAt)
			.files(fileHashInfoList)
			.build();
	}
}
