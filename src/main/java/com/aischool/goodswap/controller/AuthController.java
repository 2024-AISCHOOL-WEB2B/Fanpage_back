package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.auth.LoginRequestDTO;
import com.aischool.goodswap.DTO.auth.LoginResponseDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetCodeRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetCodeValidationResponseDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordResetResponseDTO;
import com.aischool.goodswap.DTO.auth.PasswordUpdateRequestDTO;
import com.aischool.goodswap.DTO.auth.PasswordUpdateResponseDTO;
import com.aischool.goodswap.DTO.auth.SignUpRequestDTO;
import com.aischool.goodswap.DTO.auth.SignUpResponseDTO;
import com.aischool.goodswap.service.auth.UserService;
import com.aischool.goodswap.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

@Tag(name = "Authentication", description = "인증 및 사용자 관리 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;  // JwtTokenUtil 인스턴스 주입

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 사용하여 사용자가 로그인합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증 오류"),
        @ApiResponse(responseCode = "404", description = "사용자 없음")
      })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            // 인증 성공 시 사용자 이메일 확인 및 토큰 생성
            return userService.findByEmail(loginRequest.getEmail())
                    .map(user -> {
                        String accessToken = jwtTokenUtil.generateAccessToken(loginRequest.getEmail());
                        String refreshToken = jwtTokenUtil.generateRefreshToken(loginRequest.getEmail());

                        // 리프레시 토큰 로그 추가
                        System.out.println("Generated refreshToken: " + refreshToken);

                        // 리프레시 토큰 저장 시도
                        userService.saveRefreshToken(loginRequest.getEmail(), refreshToken);

                        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(jwtTokenUtil.getRefreshTokenExpirationTime() / 1000)
                                .build();

                        int exprTime = (int) jwtTokenUtil.getAccessTokenExpirationTime();
                        return ResponseEntity.ok()
                                .header("Authorization", "Bearer " + accessToken)
//                                .header("Refresh-Token", refreshToken)
                                .body(LoginResponseDTO.builder()
                                        .message("로그인 성공")
                                        .accessToken(accessToken)
//                                        .refreshToken(refreshToken)
                                        .exprTime(exprTime)
                                        .build());
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(LoginResponseDTO.builder()
                                    .message("존재하지 않는 계정이거나 비밀번호가 틀렸습니다.")
                                    .build()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.builder()
                            .message("인증 오류가 발생했습니다.")
                            .build());
        }
    }

    @PostMapping("/logout")
    @Operation(
      summary = "로그아웃",
      description = "사용자의 액세스 및 리프레시 토큰을 무효화합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "로그아웃 완료"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 요청")
      }
    )
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        // Authorization 헤더의 내용 로그 출력
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.replace("Bearer ", "");

            // 토큰에서 사용자 정보 추출
            String email = jwtTokenUtil.extractUsername(accessToken);

            // refreshToken 삭제 시도
            userService.deleteRefreshToken(email);
            return ResponseEntity.ok("로그아웃 완료");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }
    }


    @PostMapping("/refresh")
    @Operation(
      summary = "Access Token 재발급",
      description = "유효한 Refresh Token을 사용하여 새로운 Access Token을 발급합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 완료"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
      }
    )
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !jwtTokenUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 refresh token입니다.");
        }

        String username = jwtTokenUtil.extractUsername(refreshToken);
        String newAccessToken = jwtTokenUtil.generateAccessToken(username);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newAccessToken)
                .body(Map.of("message", "Access token 재발급 완료"));
    }

    @PostMapping("/signup")
    @Operation(
      summary = "회원가입",
      description = "새로운 사용자를 등록합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      }
    )
    public ResponseEntity<SignUpResponseDTO<?>> signup(@RequestBody SignUpRequestDTO requestBody) {
        SignUpResponseDTO<?> result = userService.signup(requestBody);
        if (result.isResult()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    // 이메일 중복 확인 엔드포인트 추가
    @GetMapping("/check-email")
    @Operation(
      summary = "이메일 중복 확인",
      description = "사용자의 이메일이 중복되었는지 확인합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "사용 가능한 이메일"),
        @ApiResponse(responseCode = "409", description = "이메일 중복")
      }
    )
    public ResponseEntity<Void> checkEmail(@RequestParam String email) {
        if (userService.existsByUserEmail(email)) {
            // 이메일 중복일 경우 409 Conflict 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        // 사용 가능한 이메일일 경우 200 OK 반환
        return ResponseEntity.ok().build();
    }

    // 닉네임 중복 확인 엔드포인트 추가
    @GetMapping("/check-nickname")
    @Operation(
      summary = "닉네임 중복 확인",
      description = "사용자의 닉네임이 중복되었는지 확인합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
        @ApiResponse(responseCode = "409", description = "닉네임 중복")
      }
    )
    public ResponseEntity<Void> checkNickname(@RequestParam String nickname) {
        boolean nicknameExists = userService.existsByUserNickname(nickname);
        if (nicknameExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정을 위한 인증 코드 요청
    @PostMapping("/reset-password/request")
    @Operation(
      summary = "비밀번호 재설정 코드 요청",
      description = "비밀번호를 재설정하기 위한 인증 코드를 이메일로 발송합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "코드 발송 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      }
    )
    public ResponseEntity<PasswordResetResponseDTO> requestPasswordReset(@RequestBody PasswordResetRequestDTO requestDTO) {
        PasswordResetResponseDTO response = userService.sendPasswordResetCode(requestDTO);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else if (response.getMessage().contains("존재하지 않는 이메일")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 인증 코드 유효성 확인
    @PostMapping("/reset-password/validate-code")
    @Operation(
      summary = "비밀번호 재설정 코드 유효성 확인",
      description = "입력한 인증 코드의 유효성을 확인합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "코드 유효성 확인 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 코드")
      }
    )
    public ResponseEntity<PasswordResetCodeValidationResponseDTO> validateResetCode(@RequestBody PasswordResetCodeRequestDTO requestDTO) {
        PasswordResetCodeValidationResponseDTO response = userService.validateResetCode(requestDTO);
        return ResponseEntity.status(response.getStatus().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    // 새로운 비밀번호 설정
    @PostMapping("/reset-password/update")
    @Operation(
      summary = "비밀번호 재설정",
      description = "새로운 비밀번호를 설정합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      }
    )
    public ResponseEntity<PasswordUpdateResponseDTO> updatePassword(@RequestBody PasswordUpdateRequestDTO requestDTO) {
        PasswordUpdateResponseDTO response = userService.resetPassword(requestDTO);
        return ResponseEntity.status(response.getStatus().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    // Redis TTL 반환을 위한 새로운 엔드포인트 추가
    @GetMapping("/reset-password/remaining-time")
    @Operation(
      summary = "인증 코드 남은 시간 확인",
      description = "비밀번호 재설정 코드의 남은 시간을 반환합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "TTL 반환 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
      }
    )
    public ResponseEntity<Map<String, Long>> getResetCodeRemainingTime(@RequestParam String email) {
        Long remainingTime = userService.getResetCodeRemainingTime(email);
        Map<String, Long> response = new HashMap<>();
        response.put("remainingTime", remainingTime != null ? remainingTime : 0);
        return ResponseEntity.ok(response);
    }
}
