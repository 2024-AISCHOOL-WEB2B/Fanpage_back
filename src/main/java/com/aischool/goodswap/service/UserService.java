package com.aischool.goodswap.service;


import com.aischool.goodswap.DTO.*;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int REQUEST_LIMIT = 3; // 최대 요청 횟수
    private static final int BLOCK_DURATION_MINUTES = 10; // 차단 시간 (10분)
    private static final int CODE_EXPIRATION_MINUTES = 5; // 인증 코드 유효 시간 (5분)
    private static final String REQUEST_COUNT_KEY_PREFIX = "password_reset_request_count_";
    private static final String BLOCK_STATUS_KEY_PREFIX = "password_reset_block_status_";
    private static final String RESET_CODE_KEY_PREFIX = "password_reset_code_";

    // 이메일 중복 확인 메서드
    public boolean existsByUserEmail(String email) {
        return userRepository.existsByUserEmail(email);
    }

    // 닉네임 중복 확인 메서드
    public boolean existsByUserNickname(String nickname) {
        return userRepository.existsByUserNick(nickname);
    }

    // 비밀번호 유효성 검사 메서드
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // 이메일로 사용자 검색
    public Optional<User> findByEmail(String email) {
        return userRepository.findOneByUserEmailIgnoreCase(email);
    }

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken) {
        userRepository.findOneByUserEmailIgnoreCase(email).ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            System.out.println("Saving refreshToken to DB for user: " + email);

            userRepository.save(user);
            logger.info("리프레시 토큰 저장 완료: " + email);
        });
    }

    // 리프레시 토큰 삭제 (로그아웃 시 사용)
    public void deleteRefreshToken(String email) {
        userRepository.findOneByUserEmailIgnoreCase(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
            logger.info("리프레시 토큰 삭제 완료: " + email);
        });
    }

    // 회원가입 처리 메서드
    public SignUpResponseDTO<?> signup(SignUpRequestDTO dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        String confirmPassword = dto.getConfirmPassword();

        // 이메일 중복 확인
        if (userRepository.existsByUserEmail(email)) {
            return SignUpResponseDTO.setFailed("중복된 Email 입니다.");
        }

        // 비밀번호 일치 여부 확인
        if (!password.equals(confirmPassword)) {
            return SignUpResponseDTO.setFailed("비밀번호가 일치하지 않습니다.");
        }

        // User 객체 생성 및 필드 설정
        User user = User.builder()
                .userEmail(email)
                .userPw(passwordEncoder.encode(password))  // 비밀번호 암호화
                .userNick(dto.getNickname())
                .userRole(Role.USER)  // 기본 역할 설정
                .isActive(true)  // 활성 상태 설정
                .build();

        // 데이터베이스에 유저 저장 시도
        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.severe("회원 저장 실패: " + e.getMessage());
            return SignUpResponseDTO.setFailed("회원가입 중 문제가 발생했습니다.");
        }

        // 회원가입 성공 메시지 구체화
        return SignUpResponseDTO.setSuccess("회원가입이 완료되었습니다. 로그인 해주세요.");
    }

    // 비밀번호 재설정을 위한 인증 코드 생성 및 Redis 저장
    public PasswordResetResponseDTO sendPasswordResetCode(PasswordResetRequestDTO requestDTO) {
        String email = requestDTO.getEmail();

        // 차단 상태 확인
        if (isBlocked(email)) {
            Long remainingTime = redisTemplate.getExpire(BLOCK_STATUS_KEY_PREFIX + email, TimeUnit.SECONDS);
            long safeRemainingTime = remainingTime != null ? remainingTime : 0;
            return PasswordResetResponseDTO.fail("요청 횟수를 초과했습니다. " + safeRemainingTime / 60 + "분 후에 다시 시도해 주세요.");
        }

        // 요청 횟수 확인 및 업데이트
        int requestCount = getRequestCount(email);
        if (requestCount >= REQUEST_LIMIT) {
            blockUser(email);
            return PasswordResetResponseDTO.fail("요청 횟수를 초과했습니다. 10분 후에 다시 시도해 주세요.");
        }

        Optional<User> userOpt = userRepository.findByUserEmail(email);
        if (userOpt.isEmpty()) {
            return PasswordResetResponseDTO.fail("존재하지 않는 이메일입니다.");
        }

        // 인증 코드 생성 및 Redis 저장
        String resetCode = generateResetCode();
        redisTemplate.opsForValue().set(RESET_CODE_KEY_PREFIX + email, resetCode, Duration.ofMinutes(CODE_EXPIRATION_MINUTES));
        long expirationTimestamp = System.currentTimeMillis() + (CODE_EXPIRATION_MINUTES * 60 * 1000);

        // 요청 횟수 증가
        incrementRequestCount(email);

        logger.info("인증 코드 발송: " + resetCode);

        return PasswordResetResponseDTO.success("인증 코드가 이메일로 발송되었습니다.", expirationTimestamp);
    }

    // Redis를 통한 인증 코드 검증
    public PasswordResetCodeValidationResponseDTO validateResetCode(PasswordResetCodeRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        String resetCode = requestDTO.getResetCode();

        String storedCode = (String) redisTemplate.opsForValue().get(RESET_CODE_KEY_PREFIX + email);
        if (storedCode == null || !storedCode.equals(resetCode)) {
            return PasswordResetCodeValidationResponseDTO.fail("인증 코드가 유효하지 않습니다.");
        }

        return PasswordResetCodeValidationResponseDTO.success("인증에 성공했습니다.");
    }

    // 비밀번호 재설정
    public PasswordUpdateResponseDTO resetPassword(PasswordUpdateRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        String newPassword = requestDTO.getNewPassword();
        String confirmPassword = requestDTO.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            return PasswordUpdateResponseDTO.fail("비밀번호가 일치하지 않습니다.");
        }

        Optional<User> userOpt = userRepository.findByUserEmail(email);
        if (userOpt.isEmpty()) {
            return PasswordUpdateResponseDTO.fail("사용자를 찾을 수 없습니다.");
        }

        User user = userOpt.get();
        user.setUserPw(passwordEncoder.encode(newPassword));  // 비밀번호 암호화 및 설정
        redisTemplate.delete(RESET_CODE_KEY_PREFIX + email); // Redis에서 인증 코드 삭제
        userRepository.save(user);  // 변경 사항 저장

        return PasswordUpdateResponseDTO.success("비밀번호가 성공적으로 재설정되었습니다.");
    }

    // 인증 코드 생성 메서드 (숫자 및 대문자 8자리)
    private String generateResetCode() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    // 요청 횟수 확인 메서드
    private int getRequestCount(String email) {
        Object count = redisTemplate.opsForValue().get(REQUEST_COUNT_KEY_PREFIX + email);
        return count == null ? 0 : Integer.parseInt(count.toString());
    }

    // 요청 횟수 증가 메서드
    private void incrementRequestCount(String email) {
        String key = REQUEST_COUNT_KEY_PREFIX + email;
        redisTemplate.opsForValue().increment(key);

        // 최초 요청 시 10분 유효 기간 설정
        if (getRequestCount(email) == 1) {
            redisTemplate.expire(key, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    // 사용자 차단 메서드
    private void blockUser(String email) {
        String blockKey = BLOCK_STATUS_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(blockKey, "BLOCKED", Duration.ofMinutes(BLOCK_DURATION_MINUTES));
        redisTemplate.delete(REQUEST_COUNT_KEY_PREFIX + email); // 요청 횟수 초기화
    }

    // 차단 상태 확인 메서드
    private boolean isBlocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCK_STATUS_KEY_PREFIX + email));
    }
}
