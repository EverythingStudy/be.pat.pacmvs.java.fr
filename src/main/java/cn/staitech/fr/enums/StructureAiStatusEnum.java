package cn.staitech.fr.enums;

import lombok.Getter;

@Getter
public enum StructureAiStatusEnum {
    SUCCESS(0, "成功"), FAIL(1, "失败");
    private Integer code;
    private String value;

    StructureAiStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

}
