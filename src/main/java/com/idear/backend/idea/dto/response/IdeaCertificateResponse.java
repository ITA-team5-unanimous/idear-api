package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class IdeaCertificateResponse {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private String submitter;
	private String ideaTitle;
	private String submissionDate;
	private String documentHash;

	private String network;
	private String contractAddress;
	private String commit;
	private String txHash;
	private Integer blockNumber;
	private String onChainTimestamp;

	private String issuedAt;
	private String documentNumber;

	public static IdeaCertificateResponse of(
			User user,
			IdeaFile ideaFile,
			String ideaTitle,
			String network,
			String contractAddress
	) {
		return IdeaCertificateResponse.builder()
				.submitter(user.getName())
				.submissionDate(formatTimestamp(ideaFile.getRequestedTimestamp()))
				.ideaTitle(ideaTitle)
				.documentHash(ideaFile.getFileHash())
				.network(network)
				.contractAddress(contractAddress)
				.commit(ideaFile.getCommit())
				.txHash(ideaFile.getTxHash())
				.blockNumber(ideaFile.getBlockNumber())
				.onChainTimestamp(formatTimestamp(ideaFile.getRegisteredTimestamp()))
				.issuedAt(LocalDateTime.now(SEOUL_ZONE).format(DATE_TIME_FORMATTER))
				.documentNumber(String.valueOf(ideaFile.getIdeaFileId()))
				.build();
	}

	private static String formatTimestamp(Long timestamp) {
		if (timestamp == null) {
			return null;
		}
		return LocalDateTime.ofInstant(
				Instant.ofEpochMilli(timestamp),
				SEOUL_ZONE
		).format(DATE_TIME_FORMATTER);
	}
}
