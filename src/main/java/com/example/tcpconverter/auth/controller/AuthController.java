package com.example.tcpconverter.auth.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tcpconverter.auth.dto.ClientAuthRequest;
import com.example.tcpconverter.auth.service.ClientService;
import com.example.tcpconverter.auth.service.TokenService;
import com.example.tcpconverter.auth.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ClientService clientService;
    @Autowired
    private TokenService tokenService;

    // Access/Refresh 토큰 동시 발급
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestBody ClientAuthRequest request) {
        if (clientService.authenticate(request.getClient_id(), request.getClient_secret())) {
            Map<String, String> tokens = tokenService.generateTokens(request.getClient_id());
            return ResponseEntity.ok(tokens);
        } else {
            return ResponseEntity.status(401).body("인증 실패");
        }
    }

    // Refresh 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken == null) return ResponseEntity.badRequest().body("refresh_token 필요");
        if (tokenService.isBlacklisted(refreshToken)) return ResponseEntity.status(401).body("폐기된 토큰");
        Optional<String> newAccessToken = tokenService.refreshAccessToken(refreshToken);
        if (newAccessToken.isPresent()) {
            Map<String, String> result = new HashMap<>();
            result.put("access_token", newAccessToken.get());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body("refresh_token이 유효하지 않음");
        }
    }

    // 토큰 폐기(블랙리스트 등록)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String accessToken = body.get("access_token");
        String refreshToken = body.get("refresh_token");
        if (accessToken != null) tokenService.blacklistToken(accessToken);
        if (refreshToken != null) tokenService.blacklistToken(refreshToken);
        return ResponseEntity.ok("로그아웃/토큰 폐기 완료");
    }

    // 토큰 상태 조회 (블랙리스트 반영)
    @GetMapping("/token/status")
    public ResponseEntity<?> checkTokenStatus(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", jwtUtil.validateToken(token) && !tokenService.isBlacklisted(token));
        result.put("expired", jwtUtil.isTokenExpired(token));
        try {
            result.put("client_id", jwtUtil.getClientId(token));
            result.put("type", jwtUtil.getTokenType(token));
        } catch (Exception e) {
            result.put("client_id", null);
            result.put("type", null);
        }
        return ResponseEntity.ok(result);
    }
} 
