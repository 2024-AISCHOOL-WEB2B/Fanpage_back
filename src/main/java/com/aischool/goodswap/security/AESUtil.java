package com.aischool.goodswap.security;

import com.aischool.goodswap.exception.auth.EncryptionException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component // Spring의 관리 하에 두기 위해 추가
public class AESUtil {
  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

  @Value("${aes.key}") // application.yml에서 설정한 키 값을 주입받습니다.
  private String secretKey;

  private byte[] generateRandomBytes(int length) {
    byte[] bytes = new byte[length];
    SecureRandom random = new SecureRandom();
    random.nextBytes(bytes);
    return bytes;
  }

  private SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
    // 반복 횟수와 키 길이 설정
    int iterations = 600000; // 반복 횟수
    int keyLength = 256;    // AES-256 기준 (32 바이트)

    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] key = factory.generateSecret(spec).getEncoded();

    return new SecretKeySpec(key, ALGORITHM);
  }

  public String encrypt(String data) {
    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      byte[] iv = generateRandomBytes(16); // IV 생성
      byte[] salt = generateRandomBytes(16); // Salt 생성

      // Salt를 사용해 키 파생
      SecretKeySpec keySpec = deriveKey(secretKey, salt);
      IvParameterSpec ivParams = new IvParameterSpec(iv);

      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams);
      byte[] encryptedData = cipher.doFinal(data.getBytes());

      // IV, Salt와 암호문을 함께 반환 (Base64 인코딩)
      byte[] combined = new byte[iv.length + salt.length + encryptedData.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(salt, 0, combined, iv.length, salt.length);
      System.arraycopy(encryptedData, 0, combined, iv.length + salt.length, encryptedData.length);
      return Base64.getEncoder().encodeToString(combined);
    } catch (Exception e) {
      throw new EncryptionException("암호화 중 오류 발생", e);
    }
  }

  public String decrypt(String encryptedData) {
    try {
      byte[] combined = Base64.getDecoder().decode(encryptedData);
      byte[] iv = new byte[16]; // IV는 16바이트
      System.arraycopy(combined, 0, iv, 0, iv.length);

      byte[] salt = new byte[16]; // salt도 16바이트
      System.arraycopy(combined, iv.length, salt, 0, salt.length);

      byte[] cipherText = new byte[combined.length - iv.length - salt.length];
      System.arraycopy(combined, iv.length + salt.length, cipherText, 0, cipherText.length);

      // Salt를 사용해 동일한 키를 파생
      SecretKeySpec keySpec = deriveKey(secretKey, salt);
      IvParameterSpec ivParams = new IvParameterSpec(iv);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams);
      byte[] originalData = cipher.doFinal(cipherText);
      return new String(originalData);
    } catch (Exception e) {
      throw new EncryptionException("복호화 중 오류 발생", e);
    }
  }
}
