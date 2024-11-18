package com.aischool.goodswap.config;

import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.service.auth.MyUserDetailsService; // MyUserDetailsService 임포트 추가
import com.aischool.goodswap.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public SecurityConfig(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // 허용된 경로 설정
    private static final String[] WHITELIST = {
            "/", "/auth/login", "/auth/logout", "/auth/signup", "/login", "/register",
            "/css/**", "/fonts/**", "/images/**", "/js/**", "/logout", "/error",
            "/auth/check-email", "/auth/check-nickname", "/auth/reset-password",
            "/api/posts/", "/api/posts/{postId}/views", "/api/posts/{postId}/views/count", "/api/posts/{postId}/likes",
            "/api/posts/{postId}/previous", "/api/posts/{postId}/next", "/api/posts/popular-sse",
            "/swagger-ui/**","/api-test","/v3/api-docs/**","/swagger-ui.html"
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
                // JwtAuthenticationFilter가 SecurityContextPersistenceFilter보다 먼저 실행되도록 설정
                .addFilterBefore(jwtAuthenticationFilter(), SecurityContextPersistenceFilter.class)  // 명시적으로 순서 설정
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

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
    public MyUserDetailsService userDetailsService() {
        return new MyUserDetailsService(userRepository);
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
