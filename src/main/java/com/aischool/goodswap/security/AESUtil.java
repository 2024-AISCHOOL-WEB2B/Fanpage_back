package com.aischool.goodswap.security;

import com.aischool.goodswap.exception.EncryptionException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
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

  private byte[] generateIV() {
    byte[] iv = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(iv);
    return iv;
  }

  public String encrypt(String data) {
    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      byte[] iv = generateIV(); // 랜덤한 IV 생성
      byte[] salt = new byte[16]; // 16바이트 salt 생성
      SecureRandom random = new SecureRandom();
      random.nextBytes(salt); // 랜덤한 salt 값 생성

      IvParameterSpec ivParams = new IvParameterSpec(iv);
      SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
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

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      IvParameterSpec ivParams = new IvParameterSpec(iv);
      SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams);
      byte[] originalData = cipher.doFinal(cipherText);
      return new String(originalData);
    } catch (Exception e) {
      throw new EncryptionException("복호화 중 오류 발생", e);
    }
  }
}
