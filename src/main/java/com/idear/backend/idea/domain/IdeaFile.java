package com.idear.backend.idea.domain;

import java.time.LocalDateTime;

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
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE idea_file SET deleted_at = NOW() WHERE idea_file_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class IdeaFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ideaFileId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_id", nullable = false)
	private Idea idea;

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

	@Column(length = 64, nullable = false, unique = true)
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

	@Column(nullable = false)
	private Long requestedTimestamp;

	private Long registeredTimestamp;

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

	protected void attachTo(Idea idea) {
		this.idea = idea;
	}

	public void submitUserSignature(String userSignature, String serverSignature) {
		validateStatus(RegisterStatus.PENDING_USER_SIGNATURE);

		this.userSignature = userSignature;
		this.serverSignature = serverSignature;
		this.registerStatus = RegisterStatus.BLOCKCHAIN_PENDING;
	}

	private void validateStatus(RegisterStatus expected) {
		if (this.registerStatus != expected) {
			throw CustomException.of(ErrorCode.IDEA_FILE_STATUS_MISMATCH);
		}
	}
}