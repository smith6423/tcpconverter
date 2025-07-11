package com.example.tcpconverter.converter.parser;

public enum FieldType {
    OBJECT("O"),
    ARRAY("A"),
    NUMBER("N"),
    STRING("C");

    private final String code;

    FieldType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FieldType fromCode(String code) {
        for (FieldType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown field type code: " + code);
    }
}
