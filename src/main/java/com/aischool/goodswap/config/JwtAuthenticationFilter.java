package com.aischool.goodswap.config;

import com.aischool.goodswap.service.auth.MyUserDetailsService;
import com.aischool.goodswap.util.JwtTokenUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final MyUserDetailsService myUserDetailsService;
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    // 생성자에서 MyUserDetailsService를 직접 주입받음
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, MyUserDetailsService myUserDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Extracted Token: " + (token.length() > 10 ? token.substring(0, 10) + "..." : token));

            try {
                // 기존에 인증된 사용자가 있으면 더 이상 토큰을 처리하지 않음
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                if (currentAuth != null && currentAuth.getPrincipal() instanceof UserDetails) {
                    String username = currentAuth.getName();
                    logger.info("Using authenticated user's email from SecurityContextHolder: " + username);
                    // 이미 인증된 사용자가 있으면 바로 필터를 통과시킴
                    chain.doFilter(request, response);
                    return; // 이미 인증된 사용자이므로 추가 인증을 하지 않음
                }

                // 인증되지 않은 사용자라면 토큰에서 username 추출
                String username = jwtTokenUtil.extractUsername(token);
                logger.info("Extracted Username from token: " + username);

                // 유효한 토큰이면 인증 처리
                if (username != null && !username.isEmpty() && jwtTokenUtil.validateToken(token)) {
                    logger.info("Token is valid");

                    // 사용자 정보 로딩
                    UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                    logger.info("User details loaded for username: " + userDetails.getUsername());

                    // 인증 처리
                    Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set in SecurityContextHolder: " + authToken.getName());

                    // 인증이 완료되었으므로, 더 이상 필터를 실행하지 않음
                    // 추가: SecurityContextHolder에 설정된 Authentication을 확인하는 로그
                    logger.info("Current Authentication in SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());

                    // 인증이 완료되었으므로, 더 이상 필터를 실행하지 않음
                    // 추가: SecurityContextHolder에 설정된 Authentication을 확인하는 로그
                    logger.info("Current Authentication in SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());

                    // chain.doFilter 호출 전에 로그 추가
                    logger.info("Before calling chain.doFilter() to pass the request further");

                    // 필터 체인의 다음 필터로 진행
                    chain.doFilter(request, response);

                    // chain.doFilter 호출 후에도 로그 추가 (이 부분은 정상적으로 실행된다면 호출됨)
                    logger.info("After calling chain.doFilter()");

                    return;
                } else {
                    logger.warning("Invalid token: " + token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid token");
                    return;
                }
            } catch (Exception e) {
                logger.severe("Error processing token: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Authentication failed due to error: " + e.getMessage());
                return;
            }
        }

        // 필터 체인의 다음 필터로 진행
        chain.doFilter(request, response);
    }
}
