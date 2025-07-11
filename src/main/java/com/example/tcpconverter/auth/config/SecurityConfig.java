package com.example.tcpconverter.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.tcpconverter.auth.filter.JwtAuthenticationFilter;
import com.example.tcpconverter.auth.util.JwtUtil;

/**
 * Spring Security 설정 클래스
 * 
 * JWT 기반 인증/인가 시스템을 위한 보안 설정을 담당
 * 
 * 주요 기능:
 * - CSRF 비활성화 (JWT 토큰 사용으로 불필요)
 * - 인증/인가 경로 설정
 * - JWT 필터 체인 설정
 * - 기본 폼로그인과 HTTP Basic 인증 비활성화
 * 
 * @author converter Team
 * @since 1.0
 */
@Configuration
public class SecurityConfig {

    /** JWT 토큰 관련 유틸리티 */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Spring Security 필터 체인 설정
     * 
     * JWT 기반 인증 시스템을 위한 보안 필터 체인을 구성
     * 
     * 설정 내용:
     * 1. CSRF 보안 비활성화 (JWT 토큰 사용으로 불필요)
     * 2. 엔드포인트별 인증/인가 경로 설정
     * 3. 기본 인증 방식 비활성화
     * 4. JWT 필터 체인 설정
     * 
     * @param http HttpSecurity 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보안 비활성화 (JWT 토큰 사용으로 불필요)
            .csrf(csrf -> csrf.disable())
            
            // 엔드포인트별 인증/인가 경로 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // 인증 관련 API는 모든 사용자에게 허용
                .anyRequest().authenticated()                   // 나머지 모든 요청은 인증 필요
            )
            
            // 기본 폼로그인 비활성화 (JWT 토큰 사용)
            .formLogin(form -> form.disable())
            
            // HTTP Basic 인증 비활성화 (JWT 토큰 사용)
            .httpBasic(basic -> basic.disable())
            
            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), 
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
