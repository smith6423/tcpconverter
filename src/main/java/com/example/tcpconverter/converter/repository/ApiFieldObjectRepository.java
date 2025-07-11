package com.example.tcpconverter.converter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tcpconverter.converter.entity.ApiFieldObject;

/**
 * API 필드 오브젝트 Repository 인터페이스
 * 
 * ApiFieldObject 엔티티에 대한 데이터 접근 계층
 * 오브젝트/리스트 타입 필드의 하위 필드 정보 관리
 * 
 * 주요 기능:
 * - API 코드와 부모 필드명 기준 하위 필드 조회
 * - 필드 순서 기준 정렬 조회
 * - 오브젝트 구조의 계층 필드 관리
 * 
 * @author converter Team
 * @since 1.0
 */
public interface ApiFieldObjectRepository extends JpaRepository<ApiFieldObject, Long> {
    
    /**
     * 특정 API의 특정 부모 필드에 대한 하위 필드 목록을 필드 순서별로 조회
     * 
     * 사용 예시:
     * - findByApiCodeAndParentFieldNameOrderByFieldOrder("SDL_101", "Customer")
     *   → Customer 오브젝트의 하위 필드들 (SvcDs, CstmNm, RRNo 등)
     * - findByApiCodeAndParentFieldNameOrderByFieldOrder("SDL_101", "LoanList")  
     *   → LoanList 배열의 각 요소 필드들 (LoanNo, LoanAmt, LoanDate 등)
     * 
     * @param apiCode 조회할 API 코드 (예: "SDL_101")
     * @param parentFieldName 부모 필드명 (예: "Customer", "LoanList")
     * @return 필드 순서별로 정렬된 하위 필드 목록
     */
    List<ApiFieldObject> findByApiCodeAndParentFieldNameOrderByFieldOrder(String apiCode, String parentFieldName);
}
