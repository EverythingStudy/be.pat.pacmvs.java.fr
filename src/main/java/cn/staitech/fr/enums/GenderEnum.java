package cn.staitech.fr.enums;

/**
 * @author admin
 * @version 1.0
 * @since 2026/1/23
 */
public enum GenderEnum {
    male("0", "男", "male"),
    woman("1", "女", "woman");

    private final String code;
    private final String info;
    private final String infoEn;

    GenderEnum(String code, String info, String infoEn) {
        this.code = code;
        this.info = info;
        this.infoEn = infoEn;
    }

    public String getInfoEn() {
        return infoEn;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static GenderEnum getLogTypeEnum(Integer code) {
        GenderEnum typeEnum_ = null;
        for (GenderEnum typeEnum : GenderEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                typeEnum_ = typeEnum;
                break;
            }
        }
        return typeEnum_;
    }

}
