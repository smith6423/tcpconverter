package com.example.tcpconverter.converter.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.tcpconverter.converter.entity.ApiFieldObject;
import com.example.tcpconverter.converter.entity.ApiFieldSpec;
import com.example.tcpconverter.converter.repository.ApiFieldObjectRepository;
import com.example.tcpconverter.converter.repository.ApiFieldSpecRepository;

import jakarta.annotation.PostConstruct;

/**
 * API 스펙 레지스트리 서비스
 * 
 * 데이터베이스에서 API 필드 스펙 정보를 로드하여 메모리에 캐싱하는 레지스트리 패턴 구현
 * 애플리케이션 시작 시 모든 API 스펙을 로드하여 빠른 조회 성능을 제공
 * 
 * 주요 기능:
 * - API 코드별 필드 스펙 정보 캐싱
 * - 오브젝트/리스트 타입 필드의 하위 필드 정보 캐싱
 * - 필드 순서 보장 및 정렬
 * 
 * @author converter Team
 * @since 1.0
 */
@Component
public class ApiSpecRegistry {
    
    /** API 필드 스펙 Repository */
    private final ApiFieldSpecRepository repository;
    
    /** API 필드 오브젝트 Repository */
    private final ApiFieldObjectRepository objectRepository;
    
    /** 
     * API 코드별 필드 스펙 맵
     * Key: API 코드 (예: "SDL_101")
     * Value: 해당 API의 필드 스펙 목록 (필드 순서별로 정렬됨)
     */
    private final Map<String, List<ApiFieldSpec>> apiSpecMap = new HashMap<>();
    
    /** 
     * API 오브젝트 필드 맵 (3단계 구조)
     * Key1: API 코드 (예: "SDL_101")
     * Key2: 부모 필드명 (예: "Customer", "LoanList")
     * Value: 해당 오브젝트의 하위 필드 목록 (필드 순서별로 정렬됨)
     */
    private final Map<String, Map<String, List<ApiFieldObject>>> apiObjectFieldMap = new HashMap<>();

    /**
     * 생성자 - Repository 의존성 주입
     * 
     * @param repository API 필드 스펙 Repository
     * @param objectRepository API 필드 오브젝트 Repository
     */
    public ApiSpecRegistry(ApiFieldSpecRepository repository, ApiFieldObjectRepository objectRepository) {
        this.repository = repository;
        this.objectRepository = objectRepository;
    }

    /**
     * API 스펙 데이터 로드
     * 
     * 애플리케이션 시작 시 자동으로 실행되어 데이터베이스에서 모든 API 스펙 정보를
     * 메모리에 로드하고 캐싱함
     * 
     * 처리 과정:
     * 1. API 필드 스펙 데이터 로드 및 API 코드별 그룹화
     * 2. API 오브젝트 필드 데이터 로드 및 3단계 맵 구조 생성
     * 3. 필드 순서에 따른 정렬
     */
    @PostConstruct
    public void loadSpecs() {
        // 1. API 필드 스펙 로드 (API 코드, 필드 순서 오름차순으로 정렬)
        List<ApiFieldSpec> allSpecs = repository.findAllByOrderByApiCodeAscFieldOrderAsc();
        apiSpecMap.clear();
        
        // API 코드별로 그룹화한 LinkedHashMap 사용 (순서 보장)
        apiSpecMap.putAll(
                allSpecs.stream().collect(Collectors.groupingBy(
                    ApiFieldSpec::getApiCode,    // 그룹핑 키: API 코드
                    LinkedHashMap::new,          // 맵 타입: 순서 보장
                    Collectors.toList()          // 값 집합: 리스트로 수집
                ))
        );

        // 2. API 오브젝트 필드 로드
        List<ApiFieldObject> allObjects = objectRepository.findAll();
        apiObjectFieldMap.clear();
        
        // 3단계 맵 구조 생성: API코드 -> 부모필드명 -> 오브젝트필드목록
        for (ApiFieldObject obj : allObjects) {
            apiObjectFieldMap
                .computeIfAbsent(obj.getApiCode(), k -> new HashMap<>())        // API 코드 맵 생성
                .computeIfAbsent(obj.getParentFieldName(), k -> new ArrayList<>()) // 부모 필드별 리스트 생성
                .add(obj);  // 오브젝트 필드 추가
        }
        
        // 3. 모든 오브젝트 필드 목록을 필드 순서에 따라 정렬
        for (Map<String, List<ApiFieldObject>> parentMap : apiObjectFieldMap.values()) {
            for (List<ApiFieldObject> list : parentMap.values()) {
                list.sort(Comparator.comparingInt(ApiFieldObject::getFieldOrder));
            }
        }
    }

    /**
     * 특정 API 코드의 필드 스펙 목록 조회
     * 
     * @param apiCode API 코드 (예: "SDL_101")
     * @return 해당 API의 필드 스펙 목록 (필드 순서별로 정렬됨), 없으면 null
     */
    public List<ApiFieldSpec> getSpec(String apiCode) {
        return apiSpecMap.get(apiCode);
    }

    /**
     * 등록된 모든 API 코드 목록 조회
     * 
     * @return API 코드 집합
     */
    public Set<String> getApiCodes() {
        return apiSpecMap.keySet();
    }

    /**
     * 오브젝트/리스트 하위 필드 목록 반환
     * 
     * 메모리 캐시에서 특정 API의 특정 부모 필드에 대한 하위 필드 목록을 조회
     * 
     * @param apiCode API 코드 (예: "SDL_101")
     * @param parentFieldName 부모 필드명 (예: "Customer", "LoanList")
     * @return 하위 필드 목록 (필드 순서별로 정렬됨), 없으면 빈 리스트
     */
    public List<ApiFieldObject> getObjectFields(String apiCode, String parentFieldName) {
        return apiObjectFieldMap
            .getOrDefault(apiCode, Collections.emptyMap())              // API 코드가 없으면 빈 맵
            .getOrDefault(parentFieldName, Collections.emptyList());    // 부모 필드가 없으면 빈 리스트
    }
}
