package com.aischool.goodswap.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.logging.Logger;

@Service
public class EmailService {

  private static final Logger logger = Logger.getLogger(EmailService.class.getName());

  @Autowired
  private JavaMailSender mailSender;

  @Value("${gmail.username}")
  private String fromEmail;

  public void sendResetCodeEmail(String toEmail, String resetCode) {
    try {
      logger.info("Creating email message for password reset.");
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      // 이메일 제목과 발신자 설정
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("비밀번호 재설정 인증 코드");
      logger.info("Setting email subject and sender information.");

      // HTML 형식의 이메일 내용 작성
      String htmlContent = String.format(
        "<div style='font-size:16px; color:#333;'>"
          + "<p>안녕하세요,</p>"
          + "<p>요청하신 비밀번호 재설정을 위한 인증 코드는 아래와 같습니다:</p>"
          + "<p style='font-size:24px; color:#d9534f; font-weight:bold;'>%s</p>"
          + "<p>이 코드는 5분 동안 유효합니다.</p>"
          + "<p>감사합니다.</p>"
          + "</div>",
        resetCode
      );

      helper.setText(htmlContent, true); // HTML 형식으로 설정
      logger.info("Email content set with reset code.");

      mailSender.send(message);
      logger.info("Reset code email sent to: " + toEmail);

    } catch (MessagingException e) {
      logger.severe("Failed to send email due to messaging exception: " + e.getMessage());
      throw new RuntimeException("이메일 발송 중 오류가 발생했습니다. 다시 시도해 주세요.", e);
    } catch (Exception e) {
      logger.severe("Unexpected error during email sending: " + e.getMessage());
      throw new RuntimeException("이메일 발송 중 예상치 못한 오류가 발생했습니다.", e);
    }
  }
}
