package cn.staitech.fr.enums;

import cn.staitech.fr.utils.LanguageUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum ProjectStatusEnum {
    PENDING_STARTED(0, "待启动", "Initiation"),
    IN_PROCESS(1, "进行中", "Ongoing"),
    PAUSE(2, "暂停", "Pause"),
    COMPLETED(3, "已完成", "Complete");

    private final Integer code;
    private final String value;    // 中文名称
    private final String valueEn;  // 英文名称

    ProjectStatusEnum(Integer code, String value, String valueEn) {
        this.code = code;
        this.value = value;
        this.valueEn = valueEn;
    }

    // 根据语言环境获取名称
    public String getName() {
        return LanguageUtils.isEn() ? valueEn : value;
    }

    // 根据code获取枚举
    public static ProjectStatusEnum getByCode(Integer code) {
        for (ProjectStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    // 获取标准状态映射
    public static Map<Integer, String> getMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        ProjectStatusEnum::getCode,
                        status -> LanguageUtils.isEn() ? status.getValueEn() : status.getValue()
                ));
    }
}
