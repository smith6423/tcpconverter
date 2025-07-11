package com.example.tcpconverter.converter.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.tcpconverter.converter.entity.ApiFieldSpec;
import com.example.tcpconverter.converter.parser.TcpMessageParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP 메시지 파싱 서비스
 * TCP 메시지에서 API 서비스 코드를 추출하고, 해당 스펙을 이용해 메시지를 파싱
 * 비즈니스 로직과 예외 처리를 담당
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TcpMessageParseService {

    /** 전문 총길이 필드 시작 위치 (0-based index) */
    private static final int MSG_LENGTH_START = 0;
    
    /** 전문 총길이 필드 종료 위치 (0-based index, exclusive) */
    private static final int MSG_LENGTH_END = 6;
    
    /** API 서비스 코드 시작 위치 (0-based index) */
    private static final int API_SVC_CD_START = 135;
    
    /** API 서비스 코드 종료 위치 (0-based index, exclusive) */
    private static final int API_SVC_CD_END = 155;

    private final ApiSpecRegistry apiSpecRegistry;
    private final TcpMessageParser tcpMessageParser;

    /**
     * TCP 메시지 파싱 진입점
     * 1. TCP 메시지 전문 길이 검증
     * 2. TCP 메시지에서 API 서비스 코드 추출
     * 3. 해당 서비스 코드의 스펙 조회
     * 4. 스펙을 이용해 메시지 파싱
     * 
     * @param tcpMsg 파싱할 TCP 메시지
     * @return 파싱된 결과 맵
     * @throws IllegalArgumentException 메시지가 유효하지 않거나 스펙이 없는 경우
     */
    public Map<String, Object> parse(String tcpMsg) {
        log.debug("TCP 메시지 파싱 시작: 길이={}", tcpMsg.length());
        
        // 전문 길이 검증
        validateMessageLength(tcpMsg);
        
        String apiSvcCd = extractApiSvcCd(tcpMsg);
        log.debug("추출된 API 서비스 코드: {}", apiSvcCd);
        
        List<ApiFieldSpec> specList = getSpecList(apiSvcCd);
        log.debug("조회된 스펙 개수: {}", specList.size());
        
        Map<String, Object> result = tcpMessageParser.parse(specList, tcpMsg);
        log.debug("TCP 메시지 파싱 완료: 결과 필드 개수={}", result.size());
        
        return result;
    }

    /**
     * TCP 메시지 전문 길이 검증
     * 처음 6자리에 기록된 전문 길이와 실제 메시지 길이를 비교하여 검증
     * 
     * @param tcpMsg TCP 메시지
     * @throws IllegalArgumentException 메시지가 너무 짧거나 길이가 일치하지 않는 경우
     */
    private void validateMessageLength(String tcpMsg) {
        // 최소 길이 검증 (전문 길이 필드 + 기본 헤더)
        if (tcpMsg.length() < MSG_LENGTH_END) {
            throw new IllegalArgumentException(
                String.format("TCP 메시지가 너무 짧습니다. 최소 길이: %d, 실제 길이: %d", 
                    MSG_LENGTH_END, tcpMsg.length()));
        }

        // 전문 길이 필드 추출
        String lengthField = tcpMsg.substring(MSG_LENGTH_START, MSG_LENGTH_END);
        
        try {
            int expectedLength = Integer.parseInt(lengthField);
            int actualLength = tcpMsg.length();
            
            log.debug("전문 길이 검증: 예상={}, 실제={}", expectedLength, actualLength);
            
            if (expectedLength != actualLength) {
                throw new IllegalArgumentException(
                    String.format("전문 길이가 일치하지 않습니다. 헤더 길이: %d, 실제 길이: %d", 
                        expectedLength, actualLength));
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("전문 길이 필드가 숫자가 아닙니다: '%s'", lengthField));
        }
    }

    /**
     * TCP 메시지에서 API 서비스 코드 추출
     * 고정된 위치(135~155)에서 API 서비스 코드를 추출하고 검증
     * 
     * @param tcpMsg TCP 메시지
     * @return 추출된 API 서비스 코드 (trim 처리됨)
     * @throws IllegalArgumentException 메시지가 너무 짧거나 서비스 코드가 없는 경우
     */
    private String extractApiSvcCd(String tcpMsg) {
        // 메시지 길이 검증
        if (tcpMsg.length() <= API_SVC_CD_START) {
            throw new IllegalArgumentException(
                String.format("TCP 메시지가 너무 짧습니다. 최소 길이: %d, 실제 길이: %d", 
                    API_SVC_CD_START + 1, tcpMsg.length()));
        }

        // API 서비스 코드 추출
        int endIndex = Math.min(API_SVC_CD_END, tcpMsg.length());
        String apiSvcCd = tcpMsg.substring(API_SVC_CD_START, endIndex).trim();

        // 서비스 코드 존재 여부 검증
        if (apiSvcCd.isEmpty()) {
            throw new IllegalArgumentException("API 서비스 코드가 비어있습니다.");
        }

        return apiSvcCd;
    }

    /**
     * API 서비스 코드에 해당하는 스펙 리스트 조회
     * 
     * @param apiSvcCd API 서비스 코드
     * @return 해당 서비스 코드의 필드 스펙 리스트
     * @throws IllegalArgumentException 해당 서비스 코드의 스펙이 존재하지 않는 경우
     */
    private List<ApiFieldSpec> getSpecList(String apiSvcCd) {
        List<ApiFieldSpec> specList = apiSpecRegistry.getSpec(apiSvcCd);
        
        if (specList == null || specList.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("API 서비스 코드 '%s'에 해당하는 스펙이 존재하지 않습니다.", apiSvcCd));
        }
        
        return specList;
    }
} 