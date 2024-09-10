package cn.staitech.fr.constant;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description 常量
 * @date 2023/9/14 09:44:28
 */
public class Constants {
    /**
     * 下载任务状态：1、运行中，2、完成
     */
    public static final String DOWN_STATE_RUNNING = "1";
    public static final String DOWN_STATE_FINISH = "2";

    public static final Map<String, String> STATUS = MapUtil.builder(new HashMap<String, String>())
            .put("1", "待标注")
            .put("2", "标注中")
            .put("3", "标注完成")
            .put("4", "未复核")
            .put("5", "复核中")
            .put("6", "已复核")
            .put("7", "已交付")
            .build();
    public static final Map<String, String> STATUS_EN = MapUtil.builder(new HashMap<String, String>())
            .put("1", "Ready Annotation")
            .put("2", "Annotating")
            .put("3", "Complete Annotation")
            .put("4", "Not Reviewed")
            .put("5", "Under Review")
            .put("6", "Complete Review")
            .put("7", "Delivered")
            .build();
}
