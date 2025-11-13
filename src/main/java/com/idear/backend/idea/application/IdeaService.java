package com.idear.backend.idea.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.dto.request.IdeaRegisterRequest;
import com.idear.backend.idea.dto.response.IdeaFileResponse;
import com.idear.backend.idea.dto.response.IdeaResponse;
import com.idear.backend.idea.infrastructure.repository.IdeaRepository;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdeaService {

	private final IdeaRepository ideaRepository;
	private final FileStorageService fileStorageService;
	private final UserRepository userRepository;

	@Transactional
	public void registerIdea(IdeaRegisterRequest ideaRegisterRequest, List<MultipartFile> files) throws IOException {
		User user = userRepository.findById(ideaRegisterRequest.getUserId())
			.orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));
		Idea idea = Idea.registerIdea(
			user, ideaRegisterRequest.getTitle(), ideaRegisterRequest.getShortDescription(), ideaRegisterRequest.getDescription()
		);
		ideaRepository.save(idea);

		List<IdeaFile> ideaFiles = new ArrayList<>();
		if (files != null && !files.isEmpty()) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					String fileName = UUID.randomUUID().toString();
					String dir = parseExtension(file);
					IdeaFile.FileType fileType = dir.equals("image") ? IdeaFile.FileType.IMAGE : IdeaFile.FileType.FILE;
					String url = fileStorageService.uploadFile(file, fileName, dir);
					IdeaFile ideaFile = IdeaFile.registerIdeaFile(idea, file.getOriginalFilename(), fileName, url, fileType);
					ideaFiles.add(ideaFile);
				}
			}
		}

		if(!ideaFiles.isEmpty()){
			idea.addFile(ideaFiles);
		}

		//TODO blockchain 등록, hash 저장, 완료 후 status 변경
		// blockchain.prove();
	}

	@Transactional
	public void deleteIdea(Long ideaId) {
		Idea idea = ideaRepository.findById(ideaId)
			.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));

		List<IdeaFile> files = idea.getFiles();
		for(IdeaFile ideaFile : files){
			String dir = ideaFile.getFileType() == IdeaFile.FileType.IMAGE ? "image" : "file";
			fileStorageService.deleteFile(ideaFile.getFileName(), dir);
		}

		ideaRepository.delete(idea);
	}

	@Transactional(readOnly = true)
	public List<IdeaResponse> getIdeasByUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));
		List<Idea> ideas = ideaRepository.findAllByUser(user);

		List<IdeaResponse> responses = new ArrayList<>();

		for(Idea idea : ideas){
			List<IdeaFileResponse> fileResponses = idea.getFiles().stream()
				.map(ideaFile -> IdeaFileResponse.builder()
					.fileId(ideaFile.getFileId())
					.originalFileName(ideaFile.getOriginalFileName())
					.fileName(ideaFile.getFileName())
					.fileType(String.valueOf(ideaFile.getFileType()))
					.filePath(ideaFile.getFilePath())
					.build())
				.collect(Collectors.toList());

			IdeaResponse ideaResponse = IdeaResponse.builder()
				.ideaId(idea.getIdeaId())
				.title(idea.getTitle())
				.shortDescription(idea.getShortDescription())
				.description(idea.getDescription())
				.status(idea.getStatus())
				.createdAt(idea.getCreatedAt())
				.files(fileResponses)
				.build();

			responses.add(ideaResponse);
		}

		return responses;
	}


	private String parseExtension(MultipartFile file) {
		List<String> imageExtensions = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic");

		String originalFilename = file.getOriginalFilename();
		String fileExtension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		}

		boolean isImage = imageExtensions.contains(fileExtension);

		String resolvedUploadDir = isImage ? "image" : "file";

		return resolvedUploadDir;
	}
}
