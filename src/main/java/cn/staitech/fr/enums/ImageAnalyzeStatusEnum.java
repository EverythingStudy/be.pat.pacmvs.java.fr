package cn.staitech.fr.enums;

import cn.staitech.fr.utils.LanguageUtils;
import lombok.Getter;

@Getter
public enum ImageAnalyzeStatusEnum {
    FAILED(0, "失败", "Failed"),
    SUCCESS(1, "成功", "Success");

    private final Integer code;
    private final String value;    // 中文名称
    private final String valueEn;  // 英文名称

    ImageAnalyzeStatusEnum(Integer code, String value, String valueEn) {
        this.code = code;
        this.value = value;
        this.valueEn = valueEn;
    }

    // 根据语言环境获取名称
    public String getName() {
        return LanguageUtils.isEn() ? valueEn : value;
    }

    // 根据code获取枚举
    public static ImageAnalyzeStatusEnum getByCode(Integer code) {
        for (ImageAnalyzeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
