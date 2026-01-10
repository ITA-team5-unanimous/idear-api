package com.idear.backend.idea.domain;

import java.time.LocalDateTime;

import com.idear.backend.blockchain.domain.RegistrationFailureReason;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idea_files")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_files SET deleted_at = NOW() WHERE idea_file_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ideaFileId;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String filePath;

	@Column(length = 64, nullable = false)
	private String fileHash;

	@Column(length = 64, nullable = false)
	private String salt;

	@Column(length = 66, nullable = false, unique = true)
	private String commit;

	@Column(length = 255)
	private String userSignature;

	@Column(length = 255)
	private String serverSignature;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private RegisterStatus registerStatus = RegisterStatus.PENDING_USER_SIGNATURE;

	@Column(length = 100)
	private String txHash;

	private Integer blockNumber;

	@Column(nullable = false)
	private Long requestedTimestamp;

	private Long registeredTimestamp;

	@Enumerated(EnumType.STRING)
	private RegistrationFailureReason registrationFailureReason;

	@Column(length = 500)
	private String certificateUrl;

	private LocalDateTime deletedAt;

	public enum RegisterStatus {
		PENDING_USER_SIGNATURE,
		BLOCKCHAIN_PENDING,
		REGISTERED,
		FAILED
	}

	public static IdeaFile initialize(
			String originalFileName,
			String fileName,
			String filePath,
			String fileHash,
			String salt,
			String commit,
			Long requestedTimestamp
	) {
		return IdeaFile.builder()
				.originalFileName(originalFileName)
				.fileName(fileName)
				.filePath(filePath)
				.fileHash(fileHash)
				.salt(salt)
				.commit(commit)
				.requestedTimestamp(requestedTimestamp)
				.build();
	}

	public void submitUserSignature(String userSignature, String serverSignature) {
		validateStatus(RegisterStatus.PENDING_USER_SIGNATURE);

		this.userSignature = userSignature;
		this.serverSignature = serverSignature;
		this.registerStatus = RegisterStatus.BLOCKCHAIN_PENDING;
	}

	public void registrationSucceed(String txHash, Integer blockNumber, Long registeredAt){
		validateStatus(RegisterStatus.BLOCKCHAIN_PENDING);

		this.txHash = txHash;
		this.blockNumber = blockNumber;
		this.registeredTimestamp = registeredAt;
		this.registerStatus = RegisterStatus.REGISTERED;
	}

	public void registrationFailed(String txHash, RegistrationFailureReason reason){
		validateStatus(RegisterStatus.BLOCKCHAIN_PENDING);

		this.txHash = txHash;
		this.registrationFailureReason = reason;
		this.registerStatus = RegisterStatus.FAILED;
	}

	public void setCertificateUrl(String certificateUrl) {
		this.certificateUrl = certificateUrl;
	}

	private void validateStatus(RegisterStatus expected) {
		if (this.registerStatus != expected) {
			throw CustomException.of(ErrorCode.IDEA_FILE_STATUS_MISMATCH);
		}
	}
}