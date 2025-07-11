package com.example.tcpconverter.converter.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tcpconverter.converter.service.TcpMessageParseService;

import lombok.RequiredArgsConstructor;

/**
 * TCP 메시지 변환 API 컨트롤러
 * TCP 메시지를 JSON 형태로 파싱하는 REST API를 제공
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/convert")
public class ApiConvertController {
    private final TcpMessageParseService tcpMessageParseService;

    /**
     * TCP 메시지를 파싱하여 JSON 형태로 변환
     * 
     * @param tcpMsg 파싱할 TCP 메시지 (Raw String)
     * @return 파싱된 결과를 담은 Map 객체 (JSON 형태)
     */
    @PostMapping("/parse")
    public Map<String, Object> parseTcpMessage(@RequestBody String tcpMsg) {
        return tcpMessageParseService.parse(tcpMsg);
    }
}
