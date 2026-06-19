package com.agent.ops.client.skill.enums;

public enum FileType {
    FILE(1), FOLDER(2);

    private final int code;
    FileType(int code) { this.code = code; }
    public int getCode() { return code; }

    public static FileType fromCode(Integer code) {
        if (code == null) return null;
        for (FileType s : values()) if (s.code == code) return s;
        return null;
    }
}
