package cn.staitech.fr.enums;

import lombok.Getter;

@Getter
public enum JsonTaskStatusEnum {
    NO_PARSE(0, "未解析"), PARSE_ING(1, "解析中"), PARSE_SUCCESS(2, "解析成功"), PARSE_FAIL(3, "失败"), PARSE_NOT_START(4, "待开始");
    private Integer code;
    private String value;

    JsonTaskStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }
}
