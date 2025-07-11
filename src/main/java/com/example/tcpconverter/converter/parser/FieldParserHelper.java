package com.example.tcpconverter.converter.parser;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.example.tcpconverter.converter.entity.ApiFieldObject;
import com.example.tcpconverter.converter.entity.ApiFieldSpec;
import com.example.tcpconverter.converter.service.ApiSpecRegistry;

/**
 * TCP 메시지 파싱을 위한 헬퍼 클래스
 * 필드 값 추출, 타입 변환, 배열 카운트 처리 등의 공통 기능을 제공
 */
@Component
public class FieldParserHelper {

    /** 배열 카운트 필드의 접미사 */
    private static final String COUNT_FIELD_SUFFIX = "CNT";

    /**
     * 메시지에서 지정된 길이만큼 필드 값을 추출
     * 
     * @param msg 원본 메시지
     * @param position 현재 파싱 위치 (참조로 전달되어 업데이트됨)
     * @param fieldLength 추출할 필드 길이
     * @return 추출된 필드 값 (trim 처리됨)
     */
    public String extractFieldValue(String msg, int[] position, int fieldLength) {
        int end = Math.min(position[0] + fieldLength, msg.length());
        String value = msg.substring(position[0], end).trim();
        position[0] += fieldLength; // 파싱 위치 업데이트
        return value;
    }

    /**
     * 필드 값을 타입에 맞게 파싱
     * 
     * @param value 파싱할 값
     * @param fieldType 필드 타입 코드
     * @return 파싱된 값 (숫자 타입인 경우 Integer, 그 외에는 String)
     */
    public Object parseFieldValue(String value, String fieldType) {
        FieldType type = FieldType.fromCode(fieldType);
        if (type == FieldType.NUMBER) {
            Integer intValue = parseInteger(value);
            return intValue != null ? intValue : value;
        }
        return value;
    }

    /**
     * 문자열을 정수로 변환
     * 변환 실패 시 0을 반환
     * 
     * @param value 변환할 문자열
     * @return 변환된 정수 값 (실패 시 0)
     */
    public Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * API 필드 스펙 리스트를 필드 순서별로 정렬
     * 
     * @param specList 정렬할 스펙 리스트
     * @return 필드 순서별로 정렬된 리스트
     */
    public List<ApiFieldSpec> getOrderedSpecs(List<ApiFieldSpec> specList) {
        return specList.stream()
                .sorted(Comparator.comparingInt(ApiFieldSpec::getFieldOrder))
                .toList();
    }

    /**
     * API 필드 오브젝트 리스트를 필드 순서별로 정렬
     * 
     * @param children 정렬할 오브젝트 리스트
     * @return 필드 순서별로 정렬된 리스트
     */
    public List<ApiFieldObject> getOrderedObjectFields(List<ApiFieldObject> children) {
        return children.stream()
                .sorted(Comparator.comparingInt(ApiFieldObject::getFieldOrder))
                .toList();
    }

    /**
     * API 스펙 기반 배열 카운트 조회
     * 캐시된 값이 있으면 반환하고, 없으면 메시지에서 파싱
     * 
     * @param fieldName 배열 필드명
     * @param result 파싱 결과 맵 (캐시 용도)
     * @param apiCode API 코드
     * @param msg 원본 메시지
     * @param position 현재 파싱 위치
     * @param apiSpecRegistry API 스펙 레지스트리
     * @return 배열 카운트 값
     */
    public int getArrayCount(String fieldName, Map<String, Object> result, String apiCode,
                             String msg, int[] position, ApiSpecRegistry apiSpecRegistry) {
        return getCount(fieldName, result, msg, position, (countFieldName) -> {
            // ApiFieldSpec에서 카운트 필드 찾기
            List<ApiFieldSpec> specList = apiSpecRegistry.getSpec(apiCode);
            if (specList != null) {
                Optional<ApiFieldSpec> countSpec = specList.stream()
                        .filter(s -> countFieldName.equals(s.getFieldName()))
                        .findFirst();

                if (countSpec.isPresent()) {
                    return parseAndStoreCount(countFieldName, msg, position, countSpec.get().getFieldLength(), result);
                }
            }
            return 0;
        });
    }

    /**
     * API 오브젝트 기반 배열 카운트 조회
     * 캐시된 값이 있으면 반환하고, 없으면 메시지에서 파싱
     * 
     * @param fieldName 배열 필드명
     * @param result 파싱 결과 맵 (캐시 용도)
     * @param child 자식 오브젝트 정보
     * @param msg 원본 메시지
     * @param position 현재 파싱 위치
     * @param apiSpecRegistry API 스펙 레지스트리
     * @return 배열 카운트 값
     */
    public int getObjectArrayCount(String fieldName, Map<String, Object> result, ApiFieldObject child,
                                   String msg, int[] position, ApiSpecRegistry apiSpecRegistry) {
        return getCount(fieldName, result, msg, position, (countFieldName) -> {
            // ApiFieldObject에서 카운트 필드 찾기
            List<ApiFieldObject> siblings = apiSpecRegistry.getObjectFields(child.getApiCode(), child.getFieldName());
            Optional<ApiFieldObject> countField = siblings.stream()
                    .filter(s -> countFieldName.equals(s.getFieldName()))
                    .findFirst();

            if (countField.isPresent()) {
                return parseAndStoreCount(countFieldName, msg, position, countField.get().getFieldLength(), result);
            }
            return 0;
        });
    }

    /**
     * 배열 카운트 조회 공통 로직
     * 1. 캐시된 카운트 값 확인
     * 2. 없으면 파서를 통해 새로 파싱
     * 
     * @param fieldName 배열 필드명
     * @param result 파싱 결과 맵
     * @param msg 원본 메시지
     * @param position 현재 파싱 위치
     * @param countParser 카운트 파싱 함수
     * @return 배열 카운트 값
     */
    private int getCount(String fieldName, Map<String, Object> result, String msg, int[] position,
                        java.util.function.Function<String, Integer> countParser) {
        String countFieldName = fieldName + COUNT_FIELD_SUFFIX;

        // 이전 파싱된 카운트 필드가 있는지 확인 (캐시 활용)
        Integer count = extractCountFromResult(result, countFieldName);
        if (count != null) {
            return count;
        }

        // 카운트 필드를 찾아서 파싱
        return countParser.apply(countFieldName);
    }

    /**
     * 카운트 값을 메시지에서 파싱하고 결과 맵에 저장
     * 
     * @param countFieldName 카운트 필드명
     * @param msg 원본 메시지
     * @param position 현재 파싱 위치
     * @param fieldLength 필드 길이
     * @param result 파싱 결과 맵
     * @return 파싱된 카운트 값
     */
    private int parseAndStoreCount(String countFieldName, String msg, int[] position, int fieldLength, Map<String, Object> result) {
        String countValue = extractFieldValue(msg, position, fieldLength);
        int count = parseInteger(countValue);
        result.put(countFieldName, count); // 캐시를 위해 결과 맵에 저장
        return count;
    }

    /**
     * 파싱 결과 맵에서 카운트 값 추출
     * Integer 또는 String 타입을 지원
     * 
     * @param result 파싱 결과 맵
     * @param countFieldName 카운트 필드명
     * @return 추출된 카운트 값 (없으면 null)
     */
    private Integer extractCountFromResult(Map<String, Object> result, String countFieldName) {
        Object countObj = result.get(countFieldName);
        if (countObj instanceof Integer) {
            return (Integer) countObj;
        } else if (countObj instanceof String) {
            return parseInteger((String) countObj);
        }
        return null;
    }
}
