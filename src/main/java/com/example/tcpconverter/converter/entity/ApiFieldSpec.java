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
 * API 필드 스펙 엔티티
 * 
 * 동적 API 변환 시스템에서 사용되는 API 필드 스펙 정보를 저장하는 엔티티
 * 각 API의 필드 구조, 타입, 순서 등의 메타데이터를 관리
 * 
 * 테이블 구조:
 * - api_field_spec 테이블과 매핑
 * - 필드 순서 보장을 위한 fieldOrder 필드 포함
 * - 오브젝트/리스트 타입 지원
 * 
 * 필드 타입:
 * - 'O': Object (오브젝트)
 * - 'A': Array (배열/리스트)
 * - 'N': Number (숫자)
 * - 'C': Character (문자)
 * 
 * @author converter Team
 * @since 1.0
 */
@Entity
@Table(name = "api_field_spec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiFieldSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String apiCode;

    @Column(nullable = false)
    private int fieldOrder;

    @Column(nullable = false)
    private String fieldName;

    @Column
    private Integer fieldLength;

    @Column(nullable = false)
    private String fieldType;

    @Column
    private String groupName;

    @Column(nullable = false)
    private Boolean isList;
}
