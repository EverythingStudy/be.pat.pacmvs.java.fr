package cn.staitech.fr.enums;

import cn.staitech.fr.utils.LanguageUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum ColorTypeEnum {
    FLUORESCENCE(1, "荧光标记染色", "Fluorescent Labeling"),
    IMMUNOHISTOCHEMICAL(2, "免疫组织化学染色", "IHC"),
    HE_STAINING(3, "HE染色", "H&E"),
    MASSON_STAINING(4, "Masson染色", "Masson Staining"),
    VAN_GIESON_STAINING(5, "Van Gieson染色", "Van Gieson"),
    VICTORIA_BLUE_STAINING(6, "维多利亚蓝染色", "Victoria Blue"),
    SUDAN_STAINING(7, "苏丹III/IV染色", "Sudan III/IV"),
    OIL_RED_STAINING(8, "油红O染色", "Oil Red O"),
    PAS_STAINING(9, "PAS糖原染色", "PAS Glycogen"),
    AB_PAS_STAINING(10, "AB-PAS染色", "AB-PAS"),
    CONGO_RED_STAINING(11, "刚果红染色(甲醇)", "Congo Red (Methanol)"),
    TOLUIDINE_BLUE_STAINING(12, "甲苯胺蓝染色", "Toluidine Blue"),
    PRUSSIAN_BLUE_STAINING(13, "普鲁氏蓝染色", "Prussian Blue"),
    NISSL_STAINING(14, "尼氏染色", "Nissl"),
    LFB_MYELIN_STAINING(15, "LFB髓鞘染色", "LFB Myelin"),
    TUNEL_STAINING(16, "Tunel染色", "Tunel"),
    KI67(17, "Ki67", "Ki67"),
    OTHER(20, "其他", "Other"),
    ARGYROPHILIC_STAINING(21, "嗜银染色", "Silver");

    private final Integer code;
    private final String value;    // 中文名称
    private final String valueEn;  // 英文名称

    ColorTypeEnum(Integer code, String value, String valueEn) {
        this.code = code;
        this.value = value;
        this.valueEn = valueEn;
    }

    // 根据语言环境获取名称
    public String getName() {
        return LanguageUtils.isEn() ? valueEn : value;
    }

    // 根据code获取枚举
    public static ColorTypeEnum getByCode(Integer code) {
        for (ColorTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    // 获取标准状态映射
    public static Map<Integer, String> getMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        ColorTypeEnum::getCode,
                        status -> LanguageUtils.isEn() ? status.getValueEn() : status.getValue()
                ));
    }
}
