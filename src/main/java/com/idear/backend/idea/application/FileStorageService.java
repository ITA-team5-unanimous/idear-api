package com.idear.backend.idea.application;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
	String uploadFile(MultipartFile file, String fileName, String uploadDir) throws IOException;
	void deleteFile(String fileName, String uploadDir) ;
}
