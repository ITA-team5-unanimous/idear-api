package com.idear.backend.idea.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class HashUtil {

	private static final String HASH_ALGORITHM = "SHA-256";
	private static final int SALT_LENGTH = 32;

	public String generateFileHash(MultipartFile file) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] fileBytes = file.getBytes();
			byte[] hashBytes = digest.digest(fileBytes);
			return bytesToHex(hashBytes);
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to generate file hash", e);
		}
	}

	public String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_LENGTH];
		random.nextBytes(salt);
		return bytesToHex(salt);
	}

	public String generateCommit(String fileHash, String salt) {
		return sha256(fileHash + salt);
	}

	public String generateServerSignature(String userSignature, String commit, Long timestamp) {
		String data = userSignature + commit + timestamp;
		return sha256(data);
	}

	private String sha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
