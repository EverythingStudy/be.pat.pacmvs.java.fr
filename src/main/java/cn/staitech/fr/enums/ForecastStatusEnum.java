package cn.staitech.fr.enums;

import lombok.Getter;

@Getter
public enum ForecastStatusEnum {
    NO_FORECAST("0", "未预测"), FORECAST_SUCCESS("1", "预测成功"), FORECAST_FAIL("2", "预测失败"), FORECAST_ING("3", "预测中");
    private String code;
    private String value;

    ForecastStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }
}
