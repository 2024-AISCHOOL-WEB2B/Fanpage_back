package com.aischool.goodswap;

import com.aischool.goodswap.DTO.auth.PasswordResetCodeRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetCodeValidationResponseDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetResponseDTO;
import com.aischool.goodswap.DTO.auth.PasswordUpdateRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordUpdateResponseDTO;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.service.auth.EmailService;
import com.aischool.goodswap.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private EmailService emailService;

    private User testUser;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_RESET_CODE = "123456"; // 숫자 6자리 코드로 수정

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .userEmail(TEST_EMAIL)
                .userPw("encodedPassword")
                .userNick("TestUser")
                .build();

        // RedisTemplate와 ValueOperations mock 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // 비밀번호 재설정을 위한 인증 코드 발송 테스트 - 성공
    @Test
    void testSendPasswordResetCode_success() {
        when(userRepository.findByUserEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // void 메서드인 valueOperations.set(...)에 대해 doNothing() 사용
        doNothing().when(valueOperations).set(eq("password_reset_code_" + TEST_EMAIL), anyString(), eq(Duration.ofMinutes(5)));
        doNothing().when(emailService).sendResetCodeEmail(anyString(), anyString()); // emailService Mock 설정

        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO(TEST_EMAIL);
        PasswordResetResponseDTO responseDTO = userService.sendPasswordResetCode(requestDTO);

        assertEquals("SUCCESS", responseDTO.getStatus());
        assertEquals("인증 코드가 이메일로 발송되었습니다.", responseDTO.getMessage());

        // 호출 검증
        verify(valueOperations, times(1)).set(eq("password_reset_code_" + TEST_EMAIL), anyString(), eq(Duration.ofMinutes(5)));
        verify(emailService, times(1)).sendResetCodeEmail(anyString(), anyString()); // emailService 호출 검증
    }

    // 비밀번호 재설정을 위한 인증 코드 발송 테스트 - 실패 (이메일 없음)
    @Test
    void testSendPasswordResetCode_userNotFound() {
        when(userRepository.findByUserEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO(TEST_EMAIL);
        PasswordResetResponseDTO responseDTO = userService.sendPasswordResetCode(requestDTO);

        assertEquals("FAIL", responseDTO.getStatus());
        assertEquals("존재하지 않는 이메일입니다.", responseDTO.getMessage());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    // 인증 코드 검증 테스트 - 성공
    @Test
    void testValidateResetCode_success() {
        when(valueOperations.get("password_reset_code_" + TEST_EMAIL)).thenReturn(TEST_RESET_CODE);

        PasswordResetCodeRequestDTO requestDTO = new PasswordResetCodeRequestDTO(TEST_EMAIL, TEST_RESET_CODE);
        PasswordResetCodeValidationResponseDTO responseDTO = userService.validateResetCode(requestDTO);

        assertEquals("SUCCESS", responseDTO.getStatus());
        assertEquals("인증에 성공했습니다.", responseDTO.getMessage());
    }

    // 인증 코드 검증 테스트 - 실패 (잘못된 코드)
    @Test
    void testValidateResetCode_invalidCode() {
        when(valueOperations.get("password_reset_code_" + TEST_EMAIL)).thenReturn("WRONGCODE");

        PasswordResetCodeRequestDTO requestDTO = new PasswordResetCodeRequestDTO(TEST_EMAIL, TEST_RESET_CODE);
        PasswordResetCodeValidationResponseDTO responseDTO = userService.validateResetCode(requestDTO);

        assertEquals("FAIL", responseDTO.getStatus());
        assertEquals("인증 코드가 유효하지 않습니다.", responseDTO.getMessage());
    }

    // 차단 상태 확인 테스트
    @Test
    void testSendPasswordResetCode_blocked() {
        when(valueOperations.get("password_reset_request_count_" + TEST_EMAIL)).thenReturn("3");
        when(redisTemplate.getExpire("password_reset_block_status_" + TEST_EMAIL, TimeUnit.SECONDS)).thenReturn(600L);

        PasswordResetRequestDTO requestDTO = new PasswordResetRequestDTO(TEST_EMAIL);
        PasswordResetResponseDTO responseDTO = userService.sendPasswordResetCode(requestDTO);

        assertEquals("FAIL", responseDTO.getStatus());
        assertTrue(responseDTO.getMessage().contains("요청 횟수를 초과했습니다."));
    }

    // 비밀번호 재설정 테스트 - 성공
    @Test
    void testResetPassword_success() {
        when(userRepository.findByUserEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");

        PasswordUpdateRequestDTO requestDTO = new PasswordUpdateRequestDTO(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        PasswordUpdateResponseDTO responseDTO = userService.resetPassword(requestDTO);

        assertEquals("SUCCESS", responseDTO.getStatus());
        assertEquals("비밀번호가 성공적으로 재설정되었습니다.", responseDTO.getMessage());
        verify(redisTemplate).delete("password_reset_code_" + TEST_EMAIL);
        verify(userRepository).save(testUser);
    }

    // 비밀번호 재설정 테스트 - 실패 (비밀번호 불일치)
    @Test
    void testResetPassword_mismatchedPasswords() {
        PasswordUpdateRequestDTO requestDTO = new PasswordUpdateRequestDTO(TEST_EMAIL, TEST_PASSWORD, "differentPassword");

        PasswordUpdateResponseDTO responseDTO = userService.resetPassword(requestDTO);

        assertEquals("FAIL", responseDTO.getStatus());
        assertEquals("비밀번호가 일치하지 않습니다.", responseDTO.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 비밀번호 재설정 테스트 - 실패 (사용자 없음)
    @Test
    void testResetPassword_userNotFound() {
        when(userRepository.findByUserEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        PasswordUpdateRequestDTO requestDTO = new PasswordUpdateRequestDTO(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        PasswordUpdateResponseDTO responseDTO = userService.resetPassword(requestDTO);

        assertEquals("FAIL", responseDTO.getStatus());
        assertEquals("사용자를 찾을 수 없습니다.", responseDTO.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
