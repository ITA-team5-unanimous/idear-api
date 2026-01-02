package com.idear.backend.blockchain.application;

import com.idear.backend.alert.application.service.AlertService;
import com.idear.backend.blockchain.domain.RegistrationFailureReason;
import com.idear.backend.blockchain.dto.request.RegistrationResultRequest;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.idea.infrastructure.repository.IdeaFileRepository;
import com.idear.backend.idea.infrastructure.repository.IdeaRepository;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainService {

	@Value("${blockchain.gateway.url}")
	private String blockchainGatewayUrl;

	private final IdeaFileRepository ideaFileRepository;
	private final IdeaRepository ideaRepository;
	private final AlertService alertService;
	private final RestTemplate restTemplate;

	public void requestCommitRegistration(String commit, Long timestamp, String userSignature, String serverSignature) {
		try {
			String url = blockchainGatewayUrl + "/chain/file-proof/commits";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			CommitRegistrationRequest request = new CommitRegistrationRequest(
				commit,
				timestamp,
				userSignature,
				serverSignature
			);

			HttpEntity<CommitRegistrationRequest> entity = new HttpEntity<>(request, headers);

			restTemplate.postForObject(url, entity, String.class);

			log.info("Blockchain commit registration requested: commit={}, timestamp={}", commit, timestamp);
		} catch (Exception e) {
			log.error("Failed to request blockchain commit registration: commit={}, timestamp={}",
				commit, timestamp, e);
		}
	}

	@Transactional
	public void handleRegistrationResult(RegistrationResultRequest request){
		log.info("Received webhook: status={}, commit={}, txHash={}",
				request.getStatus(), request.getCommit(), request.getTxHash());

		String commit = request.getCommit();
		IdeaFile ideaFile = ideaFileRepository.findIdeaFileByCommit(commit)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_FILE_NOT_FOUND));

		switch (request.getStatus()) {
			case SUCCESS -> handleRegistrationSuccess(request, ideaFile);
			case FAILURE -> handleRegistrationFailure(request, ideaFile);
		}
	}

	private void handleRegistrationSuccess(RegistrationResultRequest request, IdeaFile ideaFile){
		if (request.getSuccessData() == null) {
			throw CustomException.of(ErrorCode.INVALID_INPUT);
		}

		String txHash = request.getTxHash();
		Long registeredAt = request.getSuccessData().getRegisteredAt();

		log.info("Processing SUCCESS: ideaFileId={}, txHash={}, registeredAt={}, blockNumber={}",
				ideaFile.getIdeaFileId(), txHash, registeredAt, request.getSuccessData().getBlockNumber());

		ideaFile.registrationSucceed(txHash, registeredAt);

		Idea idea = ideaRepository.findIdeaByFile(ideaFile)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));
		User user = idea.getUser();

		alertService.createRegistrationAlert(
				"아이디어 파일이 등록되었습니다. 내용이 맞는지 확인해주세요.",
				user,
				idea.getIdeaId(),
				ideaFile
		);

		// TODO: 성공 내용 이메일 전송
	}

	private void handleRegistrationFailure(RegistrationResultRequest request, IdeaFile ideaFile){
		if (request.getFailureData() == null) {
			log.error("FailureData is null for commit: {}", request.getCommit());
			throw CustomException.of(ErrorCode.INVALID_INPUT);
		}

		String txHash = request.getTxHash();
		RegistrationFailureReason reason = request.getFailureData().getReason();

		log.warn("Processing FAILURE: ideaFileId={}, txHash={}, reason={}, error={}",
				ideaFile.getIdeaFileId(), txHash, reason, request.getFailureData().getError());

		ideaFile.registrationFailed(txHash, reason);

		Idea idea = ideaRepository.findIdeaByFile(ideaFile)
				.orElseThrow(() -> CustomException.of(ErrorCode.IDEA_NOT_FOUND));
		User user = idea.getUser();

		alertService.createRegistrationAlert(
				"아이디어 파일 등록이 실패하였습니다. 상세 내역을 확인해주세요.",
				user,
				idea.getIdeaId(),
				ideaFile
		);

		// TODO: 실패 내용 이메일 전송
	}

	private record CommitRegistrationRequest(
		String commit,
		Long timestamp,
		String userSignature,
		String serverSignature
	) {}
}
