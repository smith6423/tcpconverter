package com.example.tcpconverter.auth.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tcpconverter.auth.entity.RefreshToken;
import com.example.tcpconverter.auth.entity.TokenBlacklist;
import com.example.tcpconverter.auth.repository.RefreshTokenRepository;
import com.example.tcpconverter.auth.repository.TokenBlacklistRepository;
import com.example.tcpconverter.auth.util.JwtUtil;

@Service
public class TokenService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    // Access/Refresh 토큰 동시 발급
    public Map<String, String> generateTokens(String clientId) {
        // 기존 refresh 토큰 삭제
        //refreshTokenRepository.deleteByClientId(clientId);
        String accessToken = jwtUtil.generateAccessToken(clientId);
        String refreshToken = jwtUtil.generateRefreshToken(clientId);
        // refresh 토큰 저장
        RefreshToken entity = new RefreshToken();
        entity.setClientId(clientId);
        entity.setRefreshToken(refreshToken);
        entity.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationMs() / 1000));
        refreshTokenRepository.save(entity);
        Map<String, String> result = new HashMap<>();
        result.put("access_token", accessToken);
        result.put("refresh_token", refreshToken);
        return result;
    }

    // refresh 토큰으로 access 토큰 재발급
    public Optional<String> refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || jwtUtil.isTokenExpired(refreshToken)) return Optional.empty();
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) return Optional.empty();
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (tokenOpt.isEmpty()) return Optional.empty();
        String clientId = jwtUtil.getClientId(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(clientId);
        return Optional.of(newAccessToken);
    }

    // 토큰 블랙리스트 등록 (폐기)
    public void blacklistToken(String token) {
        TokenBlacklist entity = new TokenBlacklist();
        entity.setToken(token);
        entity.setBlacklistedAt(LocalDateTime.now());
        tokenBlacklistRepository.save(entity);
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
} 
