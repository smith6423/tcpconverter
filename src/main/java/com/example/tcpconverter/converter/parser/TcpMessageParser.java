package com.example.tcpconverter.converter.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.tcpconverter.converter.entity.ApiFieldObject;
import com.example.tcpconverter.converter.entity.ApiFieldSpec;
import com.example.tcpconverter.converter.service.ApiSpecRegistry;

import lombok.RequiredArgsConstructor;

/**
 * TCP 메시지 파서
 * API 스펙 기반으로 TCP 메시지 구조를 파싱
 * 중첩 객체, 배열, 원시 타입을 모두 지원
 */
@Component
@RequiredArgsConstructor
public class TcpMessageParser {

    private final ApiSpecRegistry apiSpecRegistry;
    private final FieldParserHelper fieldParserHelper;

    /**
     * TCP 메시지 파싱 진입점
     * 
     * @param specList API 필드 스펙 리스트
     * @param message 파싱할 TCP 메시지
     * @return 파싱 결과 맵
     */
    public Map<String, Object> parse(List<ApiFieldSpec> specList, String message) {
        int[] position = {0}; // 파싱 위치를 참조할 배열
        return parseFields(specList, message, position);
    }

    /**
     * API 필드 스펙 리스트를 순서별로 파싱
     * 
     * @param specList 파싱할 스펙 리스트
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @return 파싱된 결과 맵
     */
    private Map<String, Object> parseFields(List<ApiFieldSpec> specList, String msg, int[] position) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<ApiFieldSpec> orderedSpecs = fieldParserHelper.getOrderedSpecs(specList);

        for (ApiFieldSpec spec : orderedSpecs) {
            parseField(spec, msg, position, result);
        }

        return result;
    }

    /**
     * 단일 API 필드 스펙 파싱
     * 필드 타입에 따라 적절한 파싱 메서드를 호출
     * 
     * @param spec 파싱할 필드 스펙
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseField(ApiFieldSpec spec, String msg, int[] position, Map<String, Object> result) {
        FieldType fieldType = FieldType.fromCode(spec.getFieldType());
        switch (fieldType) {
            case OBJECT:
                parseObjectField(spec, msg, position, result);
                break;
            case ARRAY:
                parseArrayField(spec, msg, position, result);
                break;
            default:
                parsePrimitiveField(spec, msg, position, result);
        }
    }

    /**
     * 객체 필드 파싱 (ApiFieldSpec 기반)
     * 
     * @param spec 객체 필드 스펙
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseObjectField(ApiFieldSpec spec, String msg, int[] position, Map<String, Object> result) {
        List<ApiFieldObject> children = apiSpecRegistry.getObjectFields(spec.getApiCode(), spec.getFieldName());
        result.put(spec.getFieldName(), parseObjectFields(children, msg, position));
    }

    /**
     * 배열 필드 파싱 (ApiFieldSpec 기반)
     * 카운트 필드를 먼저 파싱하여 배열 크기를 결정
     * 
     * @param spec 배열 필드 스펙
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseArrayField(ApiFieldSpec spec, String msg, int[] position, Map<String, Object> result) {
        String fieldName = spec.getFieldName();
        int count = fieldParserHelper.getArrayCount(fieldName, result, spec.getApiCode(), msg, position, apiSpecRegistry);

        List<Map<String, Object>> array = parseArrayElements(spec.getApiCode(), fieldName, count, msg, position);
        result.put(fieldName, array);
    }

    /**
     * 원시 타입 필드 파싱 (ApiFieldSpec 기반)
     * 
     * @param spec 원시 타입 필드 스펙
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parsePrimitiveField(ApiFieldSpec spec, String msg, int[] position, Map<String, Object> result) {
        String value = fieldParserHelper.extractFieldValue(msg, position, spec.getFieldLength());
        Object parsedValue = fieldParserHelper.parseFieldValue(value, spec.getFieldType());
        result.put(spec.getFieldName(), parsedValue);
    }

    /**
     * 객체 필드 리스트를 순서별로 파싱
     * 
     * @param children 파싱할 객체 필드 리스트
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @return 파싱된 결과 맵
     */
    private Map<String, Object> parseObjectFields(List<ApiFieldObject> children, String msg, int[] position) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<ApiFieldObject> orderedChildren = fieldParserHelper.getOrderedObjectFields(children);

        for (ApiFieldObject child : orderedChildren) {
            parseObjectField(child, msg, position, result);
        }

        return result;
    }

    /**
     * 단일 객체 필드 파싱 (ApiFieldObject 기반)
     * 필드 타입에 따라 적절한 파싱 메서드를 호출
     * 
     * @param child 파싱할 객체 필드
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseObjectField(ApiFieldObject child, String msg, int[] position, Map<String, Object> result) {
        FieldType fieldType = FieldType.fromCode(child.getFieldType());
        switch (fieldType) {
            case OBJECT:
                parseNestedObject(child, msg, position, result);
                break;
            case ARRAY:
                parseNestedArray(child, msg, position, result);
                break;
            default:
                parseNestedPrimitive(child, msg, position, result);
        }
    }

    /**
     * 중첩된 객체 필드 파싱
     * 
     * @param child 중첩 객체 필드
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseNestedObject(ApiFieldObject child, String msg, int[] position, Map<String, Object> result) {
        List<ApiFieldObject> subChildren = apiSpecRegistry.getObjectFields(child.getApiCode(), child.getFieldName());
        result.put(child.getFieldName(), parseObjectFields(subChildren, msg, position));
    }

    /**
     * 중첩된 배열 필드 파싱
     * 
     * @param child 중첩 배열 필드
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseNestedArray(ApiFieldObject child, String msg, int[] position, Map<String, Object> result) {
        String fieldName = child.getFieldName();
        int count = fieldParserHelper.getObjectArrayCount(fieldName, result, child, msg, position, apiSpecRegistry);

        List<Map<String, Object>> array = parseArrayElements(child.getApiCode(), fieldName, count, msg, position);
        result.put(fieldName, array);
    }

    /**
     * 중첩된 원시 타입 필드 파싱
     * 
     * @param child 중첩 원시 타입 필드
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @param result 파싱 결과를 저장할 맵
     */
    private void parseNestedPrimitive(ApiFieldObject child, String msg, int[] position, Map<String, Object> result) {
        String value = fieldParserHelper.extractFieldValue(msg, position, child.getFieldLength());
        Object parsedValue = fieldParserHelper.parseFieldValue(value, child.getFieldType());
        result.put(child.getFieldName(), parsedValue);
    }

    /**
     * 배열 소모를 파싱하는 공통 메서드
     * parseArrayField와 parseNestedArrayField의 중복 코드를 제거
     * 
     * @param apiCode API 코드
     * @param fieldName 필드명
     * @param count 배열 크기
     * @param msg TCP 메시지
     * @param position 현재 파싱 위치
     * @return 파싱된 배열 소모
     */
    private List<Map<String, Object>> parseArrayElements(String apiCode, String fieldName, int count, String msg, int[] position) {
        List<ApiFieldObject> children = apiSpecRegistry.getObjectFields(apiCode, fieldName);
        List<Map<String, Object>> array = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            array.add(parseObjectFields(children, msg, position));
        }

        return array;
    }
}
