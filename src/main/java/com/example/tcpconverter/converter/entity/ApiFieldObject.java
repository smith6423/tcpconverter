package com.example.tcpconverter.converter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API 필드 오브젝트 엔티티
 * 
 * 오브젝트/리스트 타입 필드의 하위 필드 정보를 저장하는 엔티티
 * ApiFieldSpec에서 정의된 오브젝트나 리스트 타입 필드의 세부 구조를 관리
 * 
 * 사용 예시:
 * - Customer 오브젝트의 하위 필드들 (SvcDs, CstmNm, RRNo 등)
 * - LoanList 배열의 각 요소 필드들 (LoanNo, LoanAmt, LoanDate 등)
 * 
 * 테이블 구조:
 * - api_field_object 테이블과 매핑
 * - parentFieldName으로 상위 필드와 연결
 * - fieldOrder로 하위 필드 순서 보장
 * 
 * @author converter Team
 * @since 1.0
 */
@Entity
@Table(name = "api_field_object")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiFieldObject {

    /** 기본 키 (자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** API 코드 */
    @Column(nullable = false)
    private String apiCode;

    /** 부모 필드명 */
    @Column(nullable = false)
    private String parentFieldName;

    /** 필드 순서 */
    @Column(nullable = false)
    private int fieldOrder;

    /** 필드명 */
    @Column(nullable = false)
    private String fieldName;

    /** 필드 길이 */
    @Column(nullable = false)
    private int fieldLength;

    /** 필드 타입: 'O', 'A', 'N', 'C' */
    @Column(nullable = false)
    private String fieldType;

    /** 리스트 여부 */
    @Column(nullable = false)
    private Boolean isList;
}
