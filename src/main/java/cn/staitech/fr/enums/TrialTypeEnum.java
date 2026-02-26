package cn.staitech.fr.enums;

import cn.staitech.fr.utils.LanguageUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum TrialTypeEnum {
//    RESERVE(1, "储备", "Reserve"),
    SINGLE_DOSING_TOXICITY(2, "单次给药毒性试验", "Single Dose Toxicity Study"),
    REPEATED_DOSING_TOXICITY(3, "重复给药毒性试验", "Repeated Dose Toxicity Study"),
    DOSE_FINDING_TOXICITY(4, "剂量探索毒性试验", "Dose Rang Finding Study"),
    IRRITATION_TEST(5, "刺激试验", "Irritation Test"),
    ALLERGY_TEST(6, "过敏试验", "Allergy Test"),
    CARCINOGENICITY_STUDY(7, "致癌试验", "Carcinogenicity Study"),
/*    FERTILITY_SEGMENT1(8, "Fertility (Segment Ⅰ)", "Fertility (Segment Ⅰ)"),
    EMBRYO_TOXICITY_SEGMENT2(9, "Embryo-toxicity (Segment Ⅱ)", "Embryo-toxicity (Segment Ⅱ)"),
    PERI_POSTNATAL_SEGMENT3(10, "Peri ＆ Post-natal (Segment Ⅲ)", "Peri ＆ Post-natal (Segment Ⅲ)"),
    MONITORING_EXPERIMENT(11, "监测实验", "Monitoring Experiment"),*/
    SAFETY_PHARMACOLOGY(12, "安全药理", "Safety Pharmacology Study"),
    TUMORIGENICITY_STUDY(13, "致瘤性试验", "Tumorigenicity Study"),
    MICRONUCLEUS_TEST(14, "微核试验", "Micronucleus Test"),
    AMES_TEST(15, "AMES试验", "Ames Test"),
    CHROMOSOMAL_ABERATION_TEST(16, "染色体畸变试验", "Chromosomal aberration Test"),
    /*SINGLE_DOSING_PHA(17, "单次给药药代试验", "Single Dose Pharmacokinetic Study"),
    REPEATED_DOSING_PHA(18, "重复给药药代试验", "Repeated Dose Pharmacokinetic Study"),*/
    PYROGEN_TEST(19, "热原试验", "Pyrogen"),
    OTHER_STUDIES(20, "其他试验", "Other Studies");

    private final Integer code;
    private final String value;    // 中文名称
    private final String valueEn;  // 英文名称

    TrialTypeEnum(Integer code, String value, String valueEn) {
        this.code = code;
        this.value = value;
        this.valueEn = valueEn;
    }

    // 根据语言环境获取名称
    public String getName() {
        return LanguageUtils.isEn() ? valueEn : value;
    }

    // 根据code获取枚举
    public static TrialTypeEnum getByCode(Integer code) {
        for (TrialTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static Map<Integer, String> getMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        TrialTypeEnum::getCode,
                        status -> LanguageUtils.isEn() ? status.getValueEn() : status.getValue()
                ));
    }
}
