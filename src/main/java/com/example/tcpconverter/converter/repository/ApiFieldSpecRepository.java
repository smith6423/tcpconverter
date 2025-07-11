package com.example.tcpconverter.converter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tcpconverter.converter.entity.ApiFieldSpec;

/**
 * API 필드 스펙 Repository 인터페이스
 * 
 * ApiFieldSpec 엔티티에 대한 데이터 접근 계층
 * Spring Data JPA를 사용하여 기본 CRUD 및 커스텀 쿼리 메서드를 제공
 * 
 * 주요 기능:
 * - API 코드별 필드 스펙 조회
 * - 필드 순서 기준 정렬 조회
 * - 전체 스펙 데이터 정렬 조회
 * 
 * @author converter Team
 * @since 1.0
 */
public interface ApiFieldSpecRepository extends JpaRepository<ApiFieldSpec, Long> {
    
    /**
     * 특정 API 코드의 필드 스펙 목록을 필드 순서별로 조회
     * 
     * @param apiCode 조회할 API 코드 (예: "SDL_101")
     * @return 필드 순서별로 정렬된 API 필드 스펙 목록
     */
    List<ApiFieldSpec> findByApiCodeOrderByFieldOrder(String apiCode);
    
    /**
     * 모든 API 필드 스펙을 API 코드와 필드 순서 기준으로 정렬하여 조회
     * 
     * ApiSpecRegistry에서 애플리케이션 시작 시 모든 스펙을 로드할 때 사용
     * API 코드별로 그룹화하고 그룹 내에서 필드 순서별로 정렬
     * 
     * @return API 코드 및 필드 순서별 정렬된 전체 API 필드 스펙 목록
     */
    List<ApiFieldSpec> findAllByOrderByApiCodeAscFieldOrderAsc();
} 
