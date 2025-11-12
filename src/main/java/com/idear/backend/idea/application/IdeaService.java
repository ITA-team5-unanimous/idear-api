package com.idear.backend.idea.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.dto.request.IdeaRegisterRequest;
import com.idear.backend.idea.infrastructure.repository.IdeaFileRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdeaService {

	private final IdeaRepository ideaRepository;
	private final IdeaFileRepository ideaFileRepository;
	private final FileStorageService fileStorageService;

	@Transactional
	public void registerIdea(IdeaRegisterRequest ideaRegisterRequest, List<MultipartFile> files) throws IOException {
		Idea idea = Idea.registerIdea(
			ideaRegisterRequest.getTitle(), ideaRegisterRequest.getShortDescription(), ideaRegisterRequest.getShortDescription()
		);
		ideaRepository.save(idea);

		List<IdeaFile> ideaFiles = new ArrayList<>();
		if (files != null && !files.isEmpty()) {
			for(MultipartFile i:files)
				System.out.println(i.getOriginalFilename());

			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					String fileName = String.valueOf(UUID.nameUUIDFromBytes(file.getName().getBytes()));
					String dir = parseExtension(file);
					IdeaFile.FileType fileType = dir.equals("image") ? IdeaFile.FileType.IMAGE : IdeaFile.FileType.FILE;
					String url = fileStorageService.uploadFile(file, fileName, dir);
					IdeaFile ideaFile = IdeaFile.registerIdeaFile(idea, fileName, url, fileType);
					ideaFiles.add(ideaFile);
				}
			}
		}

		if(!ideaFiles.isEmpty()){
			idea.addFile(ideaFiles);
			ideaRepository.save(idea);
		}

		//TODO blockchain 등록, hash 저장, 완료 후 status 변경
		// blockchain.prove();
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
