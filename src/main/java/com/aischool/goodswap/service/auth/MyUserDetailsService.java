package com.aischool.goodswap.service.auth;

import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 로거 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", username);

        // 이메일을 통해 사용자 조회
        Optional<User> userOptional = userRepository.findByUserEmail(username);

        if (userOptional.isEmpty()) {
            // 사용자를 찾을 수 없는 경우 로그 추가
            logger.error("User not found with email: {}", username);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();
        logger.info("User found: {} (Role: {})", user.getUserEmail(), user.getUserRole());

        // 권한을 리스트로 반환 (ROLE_ 접두사를 붙여줍니다)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())); // 예: ROLE_USER

        // UserDetails를 반환 (이메일, 비밀번호, 권한)
        logger.info("Returning UserDetails for email: {}", user.getUserEmail());
        return new org.springframework.security.core.userdetails.User(user.getUserEmail(), user.getPassword(), authorities);
    }
}
