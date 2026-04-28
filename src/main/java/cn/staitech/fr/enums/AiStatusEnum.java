package cn.staitech.fr.enums;

public enum AiStatusEnum {

    NOT_ANALYZED(0, "未分析"),
    ORGAN_RECOGNITION(1, "脏器识别中"),
    ABNORMAL_ORGAN_RECOGNITION(2, "脏器识别异常"),
    ORGAN_IDENTIFICATION_COMPLETED(3, "脏器识别完成");
    private int code;
    private String value;

    AiStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
