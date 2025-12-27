package com.idear.backend.idea.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

@Component
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ServerSignatureService {

	private static final String HASH_ALGORITHM = "SHA-256";
	private static final String CURVE_NAME = "secp256r1";

	@Value("${blockchain.server.private-key}")
	private String serverPrivateKey;

	private String serverPublicKey;
	private PrivateKey privateKey;

	@PostConstruct
	public void init() {
		try {
			// 비밀키로부터 공개키 계산 (P-256)
			String cleanPrivateKey = serverPrivateKey.startsWith("0x")
				? serverPrivateKey.substring(2)
				: serverPrivateKey;

			BigInteger privateKeyInt = new BigInteger(cleanPrivateKey, 16);

			// P-256 파라미터 가져오기
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
			parameters.init(new ECGenParameterSpec(CURVE_NAME));
			ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);

			// 공개키 계산
			ECPoint publicPoint = multiplyPoint(ecParameters.getGenerator(), privateKeyInt, ecParameters);

			// 공개키 바이트 생성
			byte[] xBytes = publicPoint.getAffineX().toByteArray();
			byte[] yBytes = publicPoint.getAffineY().toByteArray();

			byte[] publicKeyBytes = new byte[65];
			publicKeyBytes[0] = 0x04;
			System.arraycopy(trimLeadingZeros(xBytes), 0, publicKeyBytes, 1 + (32 - Math.min(32, xBytes.length)), Math.min(32, xBytes.length));
			System.arraycopy(trimLeadingZeros(yBytes), 0, publicKeyBytes, 33 + (32 - Math.min(32, yBytes.length)), Math.min(32, yBytes.length));

			this.serverPublicKey = "0x" + bytesToHex(publicKeyBytes);

			// 서명용 PrivateKey 객체 생성
			ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyInt, ecParameters);
			KeyFactory keyFactory = KeyFactory.getInstance("EC");
			this.privateKey = keyFactory.generatePrivate(privateKeySpec);

		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize crypto keys", e);
		}
	}

	public String generateServerSignature(String commit, Long timestamp, String userSignature) {
		try {
			// 서명할 데이터: commit | timestamp | userSignature
			String data = String.join("|", commit, timestamp.toString(), userSignature);
			byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

			// SHA-256 해싱
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hash = digest.digest(dataBytes);

			// ECDSA 서명 생성 (P-256)
			Signature signature = Signature.getInstance("SHA256withECDSA");
			signature.initSign(privateKey);
			signature.update(hash);
			byte[] signatureBytes = signature.sign();

			return "0x" + bytesToHex(signatureBytes);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate server signature", e);
		}
	}

	public String getServerPublicKey() {
		return serverPublicKey;
	}

	private ECPoint multiplyPoint(ECPoint g, BigInteger k, ECParameterSpec params) {
		ECPoint result = null;
		ECPoint temp = g;

		byte[] kBytes = k.toByteArray();
		for (int i = kBytes.length - 1; i >= 0; i--) {
			for (int j = 0; j < 8; j++) {
				if ((kBytes[i] & (1 << j)) != 0) {
					result = (result == null) ? temp : addPoints(result, temp, params);
				}
				temp = addPoints(temp, temp, params);
			}
		}
		return result;
	}

	private ECPoint addPoints(ECPoint p1, ECPoint p2, ECParameterSpec params) {
		if (p1 == null) return p2;
		if (p2 == null) return p1;

		BigInteger p = ((ECFieldFp) params.getCurve().getField()).getP();
		BigInteger x1 = p1.getAffineX();
		BigInteger y1 = p1.getAffineY();
		BigInteger x2 = p2.getAffineX();
		BigInteger y2 = p2.getAffineY();

		BigInteger lambda;
		if (x1.equals(x2)) {
			if (y1.equals(y2)) {
				BigInteger a = params.getCurve().getA();
				lambda = x1.pow(2).multiply(BigInteger.valueOf(3)).add(a)
					.multiply(y1.multiply(BigInteger.valueOf(2)).modInverse(p)).mod(p);
			} else {
				return null;
			}
		} else {
			lambda = y2.subtract(y1).multiply(x2.subtract(x1).modInverse(p)).mod(p);
		}

		BigInteger x3 = lambda.pow(2).subtract(x1).subtract(x2).mod(p);
		BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p);

		return new ECPoint(x3, y3);
	}

	private byte[] trimLeadingZeros(byte[] bytes) {
		int i = 0;
		while (i < bytes.length - 1 && bytes[i] == 0) {
			i++;
		}
		byte[] result = new byte[bytes.length - i];
		System.arraycopy(bytes, i, result, 0, result.length);
		return result;
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
