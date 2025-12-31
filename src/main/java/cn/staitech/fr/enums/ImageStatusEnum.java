package cn.staitech.fr.enums;

import cn.staitech.fr.utils.LanguageUtils;
import lombok.Getter;

@Getter
public enum ImageStatusEnum {
    UPLOADING(0, "上传中", "Uploading"),
    UPLOAD_FAILED(1, "上传失败", "Upload Failed"),
    PARSING(2, "解析中", "Parsing"),
    PARSE_FAILED(3, "解析失败", "Parse Failed"),
    INFO_PARSING(5, "信息解析中", "Info Parsing"),
    INFO_PARSE_FAILED(6, "信息解析失败", "Info Parse Failed"),
    PROCESSING(7, "处理中", "Processing"),
    PROCESSING_FAILED(8, "处理失败", "Processing Failed"),
    AVAILABLE(4, "可用", "Available");

    private final Integer code;
    private final String value;    // 中文名称
    private final String valueEn;  // 英文名称

    ImageStatusEnum(Integer code, String value, String valueEn) {
        this.code = code;
        this.value = value;
        this.valueEn = valueEn;
    }

    // 根据语言环境获取名称
    public String getName() {
        return LanguageUtils.isEn() ? valueEn : value;
    }

    // 其他方法保持不变
    public static ImageStatusEnum getByCode(Integer code) {
        for (ImageStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
