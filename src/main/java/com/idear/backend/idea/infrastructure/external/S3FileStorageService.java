package com.idear.backend.idea.infrastructure.external;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.application.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

	private static final String S3_BUCKET_NAME = "idear-file";
	private final AmazonS3 amazonS3;

	@Override
	public String uploadFile(MultipartFile file, String fileName, String uploadDir){
		String key = uploadDir + "/" + fileName;

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(file.getSize());

		try {
			PutObjectRequest putObjectRequest =
				new PutObjectRequest(S3_BUCKET_NAME, key, file.getInputStream(), objectMetadata);
			amazonS3.putObject(putObjectRequest);
		} catch (IOException e) {
			throw CustomException.of(ErrorCode.FILE_UPLOAD_ERROR);
		}

		String accessUrl = amazonS3.getUrl(S3_BUCKET_NAME, key).toString();

		return accessUrl;
	}

	@Override
	public void deleteFile(String storedFileName, String uploadDir) {
		String key = uploadDir + storedFileName;
		try {
			boolean isObjectExist = amazonS3.doesObjectExist(S3_BUCKET_NAME, key);
			if (!isObjectExist){
				throw CustomException.of(ErrorCode.FILE_NOTFOUND_ERROR);
			}
			amazonS3.deleteObject(S3_BUCKET_NAME, key);

		} catch (CustomException e){
			throw e;
		} catch (Exception e) {
			throw CustomException.of(ErrorCode.FILE_DELETE_ERROR);
		}
	}
}
