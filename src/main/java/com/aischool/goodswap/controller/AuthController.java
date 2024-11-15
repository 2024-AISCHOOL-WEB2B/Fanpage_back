package com.aischool.goodswap.controller;

// src/main/com/aischool/goodswap/controller/AuthController
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
              .orElseGet(() -> ResponseEntity.status(404)
                .body(LoginResponseDTO.builder()
                  .message("존재하지 않는 계정이거나 비밀번호가 틀렸습니다.")
                  .build()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
              .body(LoginResponseDTO.builder()
                .message("인증 오류가 발생했습니다.")
                .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        // Authorization 헤더의 내용 로그 출력
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.replace("Bearer ", "");
            System.out.println("Received accessToken in logout request: " + accessToken);

            // 토큰에서 사용자 정보 추출
            String email = jwtTokenUtil.extractUsername(accessToken);

            // refreshToken 삭제 시도
            userService.deleteRefreshToken(email);
            System.out.println("Logout successful for user: " + email);
            return ResponseEntity.ok("로그아웃 완료");
        } else {
            System.out.println("Authorization header is missing or invalid.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }
    }



    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
          .filter(cookie -> "refreshToken".equals(cookie.getName()))
          .findFirst()
          .map(Cookie::getValue)
          .orElse(null);

        if (refreshToken == null || !jwtTokenUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("유효하지 않은 refresh token입니다.");
        }

        String username = jwtTokenUtil.extractUsername(refreshToken);
        String newAccessToken = jwtTokenUtil.generateAccessToken(username);

        return ResponseEntity.ok()
          .header("Authorization", "Bearer " + newAccessToken)
          .body(Map.of("message", "Access token 재발급 완료"));
    }

    @PostMapping("/signup")
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
    public ResponseEntity<Void> checkNickname(@RequestParam String nickname) {
        boolean nicknameExists = userService.existsByUserNickname(nickname);
        if (nicknameExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정을 위한 인증 코드 요청
    @PostMapping("/reset-password/request")
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
    public ResponseEntity<PasswordResetCodeValidationResponseDTO> validateResetCode(@RequestBody PasswordResetCodeRequestDTO requestDTO) {
        PasswordResetCodeValidationResponseDTO response = userService.validateResetCode(requestDTO);
        return ResponseEntity.status(response.getStatus().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    // 새로운 비밀번호 설정
    @PostMapping("/reset-password/update")
    public ResponseEntity<PasswordUpdateResponseDTO> updatePassword(@RequestBody PasswordUpdateRequestDTO requestDTO) {
        PasswordUpdateResponseDTO response = userService.resetPassword(requestDTO);
        return ResponseEntity.status(response.getStatus().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }
}
