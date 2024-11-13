package com.aischool.goodswap.config;

import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.logging.Logger;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public SecurityConfig(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private static final Logger logger = Logger.getLogger(SecurityConfig.class.getName());

    // 허용된 경로 설정
    private static final String[] WHITELIST = {
            "/", "/auth/login", "/auth/logout", "/auth/signup", "/login", "/register",
            "/css/**", "/fonts/**", "/images/**", "/js/**", "/logout", "/error",
            "/auth/check-email", "/auth/check-nickname" // 이메일 및 닉네임 중복 확인 엔드포인트
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService()), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginProcessingUrl("/loginProc")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            logger.info("User successfully logged in: " + authentication.getName());
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Login Successful");
                        })
                        .failureHandler((request, response, exception) -> {
                            logger.warning("Login failed: " + exception.getMessage());
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Login Failed: " + exception.getMessage());
                        })
                );
//                .logout(logout -> logout
//                        .logoutUrl("/auth/logout")
//                        .logoutSuccessHandler((request, response, authentication) -> {
//                            logger.info("User logged out successfully");
//
//                            response.setStatus(HttpServletResponse.SC_OK);
//                            response.getWriter().write("Logged out successfully");
//                        })
//                        .invalidateHttpSession(true) // 세션 무효화 (세션 방식일 때만 적용)
//                        .deleteCookies("refreshToken") // 로그아웃 시 리프레시 토큰 쿠키 삭제
//                );

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findOneByUserEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.warning("User not found for email: " + email);
                    return new UsernameNotFoundException("User not found");
                });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // 프론트엔드 주소
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 메서드 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
