package cn.staitech.fr.enums;

import lombok.Getter;

@Getter
public enum StructureTypeEnum {
    STRUCTURE(1, "RO", "结构类型"),
    ANNOTATION_AREA(2, "ROA", "标注区域"),
    EXAMINATION_AREA(3, "ROE", "考核区域");

    private final int code;
    private final String type;
    private final String description;

    StructureTypeEnum(int code, String type, String description) {
        this.code = code;
        this.type = type;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举对象
     */
    public static StructureTypeEnum getByCode(int code) {
        for (StructureTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知结构标签类型 code: " + code);
    }

    /**
     * 根据 code 获取描述信息
     */
    public static String getDescriptionByCode(int code) {
        return getByCode(code).getDescription();
    }

    /**
     * 根据 code 获取标签类型（如 RO/ROA/ROE）
     */
    public static String getTagTypeByCode(int code) {
        return getByCode(code).getType();
    }

    /**
     * 根据 type 获取枚举对象
     */
    public static StructureTypeEnum getByType(String type) {
        for (StructureTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("未知结构标签类型 type: " + type);
    }

    /**
     * 根据 type 获取 code
     */
    public static int getCodeByType(String type) {
        return getByType(type).getCode();
    }
}