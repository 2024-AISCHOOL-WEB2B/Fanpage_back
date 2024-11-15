package com.aischool.goodswap.config;

import com.aischool.goodswap.exception.auth.InvalidTokenException;
import com.aischool.goodswap.util.JwtTokenUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.logging.Logger;

/**
 * JWT 인증 필터: 들어오는 요청에서 JWT 토큰을 처리하고 검증.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    // 생성자 주입 방식으로 JwtTokenUtil과 UserDetailsService를 주입받음
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtTokenUtil.extractUsername(token);

                // 인증되지 않은 상태에서만 사용자 정보를 가져옴
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtTokenUtil.validateToken(token)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // 인증 정보가 설정되었음을 확인하는 로그 추가
                        logger.info("SecurityContextHolder에 인증 정보 설정 완료: " + authToken.getName());
                    } else {
                        logger.warning("유효하지 않은 토큰: " + token);
                    }
                } else {
                    logger.warning("SecurityContextHolder에 이미 인증 정보가 설정되어 있습니다: " +
                            SecurityContextHolder.getContext().getAuthentication());
                }
            } catch (InvalidTokenException e) {
                logger.warning("유효하지 않은 토큰: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

