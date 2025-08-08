package cn.staitech.fr.constant;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description 业务常量
 * @date 2025/5/14 13:44:14
 */
public class Constants {

    /**
     * 项目状态(0-待启动，1-进行中，2-暂停，3-已完成，6-归档)常量
     */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_ARCHIVED = 6;

    /**
     * image 图像切片状态：0-上传中；1-上传失败；2-解析中；3-解析失败；4-解析成功
     */
    public static final String IMAGE_STATUS_ENABLE = "4";
    public static final String IMAGE_PROCESS_PARSING = "2";
    public static final String IMAGE_PROCESS_PARSE_FAIL = "3";
    public static final String IMAGE_PROCESS_UPLOAD_FAIL = "1";
    public static final String IMAGE_PROCESS_UPLOADING = "0";
    public static final Integer IMAGE_SOURCE_UPLOAD = 1;
    public static final Integer IMAGE_SOURCE_SERVER = 2;

    /**
     * 图像名称解析状态
     */
    public static final Integer IMAGE_NAME_PARSE_FAIL = 0;
    public static final Integer IMAGE_NAME_PARSE_SUCC = 1;

    /**
     * 项目成员角色常量
     */
    public static final int ROLE_ADMIN = 1;    // 机构管理员
    public static final int ROLE_OWNER = 2;    // 项目负责人
    public static final int ROLE_MEMBER = 3;   // 项目参与用户
    public static final int ROLE_OTHER = 4;    // 其他用户



    //标签类型 0:下拉筛选标签；1:自定义标签
    public static final int TAG_TYPE_DROPDOWN = 0;
    public static final int TAG_TYPE_CUSTOM = 1;

    /**
     * structure
     */
    public static final String STRUCTURE_RO = "RO";
    public static final String STRUCTURE_ROA = "ROA";
    public static final String STRUCTURE_ROE = "ROE";
    public static final Integer STRUCTURE_ROA_GROUP_NUMBER = 1;
    public static final Integer STRUCTURE_ROE_GROUP_NUMBER = 2;
    public static final Integer STRUCTURE_RO_GROUP_NUMBER = 3;

    /**
     * 下载任务状态：1、运行中，2、完成
     */
    public static final String DOWN_STATE_RUNNING = "1";
    public static final String DOWN_STATE_FINISH = "2";
    public static final String TEMP_TABLE_KEY = "TEMP_TABLE_KEY";
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
