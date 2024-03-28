package cn.staitech.fr.constant;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map
 *
 * @author wangf
 */
public class Container {

    /**
     * FileUpload - 定义一个基于多线程 的 hashmap
     */
    public static final ConcurrentHashMap<String, ConcurrentHashSet<Integer>> FILE_MAP = new ConcurrentHashMap<>();

    /**
     * Image - 原始切片 - 切片状态：0上传中、1上传失败、2解析中、3解析失败、4可用
     */
    public static final Map<Integer, String> IMAGE_STATUS_MAP = new ImmutableMap.Builder<Integer, String>()
            .put(0, "上传中")
            .put(1, "上传失败")
            .put(2, "解析中")
            .put(3, "解析失败")
            .put(4, "可用")
            .build();

    /**
     * Image - 原始切片 - 切片状态（EN）：0上传中、1上传失败、2解析中、3解析失败、4可用
     */
    public static final Map<Integer, String> IMAGE_STATUS_MAP_EN = new ImmutableMap.Builder<Integer, String>()
            .put(0, "上传中en")
            .put(1, "上传失败en")
            .put(2, "解析中en")
            .put(3, "Unavailable")
            .put(4, "Available")
            .build();

    /**
     * 项目状态
     */
    public static final Map<Integer, String> PROJECT_STATUS = new ImmutableMap.Builder<Integer, String>()
            .put(1, "待启动")
            .put(2, "进行中")
            .put(3, "暂停")
            .put(4, "已完成")
            .build();

    /**
     * 项目状态 - EN
     */
    public static final Map<Integer, String> PROJECT_STATUS_EN = new ImmutableMap.Builder<Integer, String>()
            .put(1, "Pending Started")
            .put(2, "In process")
            .put(3, "Pause")
            .put(4, "Done")
            .build();

    /**
     * 专题用户状态：0开启，1禁用 SpecialRoleUser
     */
    public static final Map<Long, String> SPECIAL_ROLE_STATUS_MAP = new ImmutableMap.Builder<Long, String>()
            .put(0L, "开启")
            .put(1L, "禁用")
            .build();

    /**
     * 专题用户状态 - EN：0开启，1禁用 SpecialRoleUser
     */
    public static final Map<Long, String> SPECIAL_ROLE_STATUS_MAP_EN = new ImmutableMap.Builder<Long, String>()
            .put(0L, "ON")
            .put(1L, "FORBIDDEN")
            .build();

    /**
     * 颜色类型
     */
    public static final Map<Integer, String> COLOR_TYPE = new ImmutableMap.Builder<Integer, String>()
            .put(1, "荧光标记染色")
            .put(2, "免疫组织化学染色")
            .put(3, "HE染色")
            .put(4, "Masson染色")
            .put(5, "Van Gieson染色")
            .put(6, "维多利亚蓝染色")
            .put(7, "苏丹III/IV染色")
            .put(8, "油红O染色")
            .put(9, "PAS糖原染色")
            .put(10, "AB-PAS染色")
            .put(11, "刚果红染色(甲醇)")
            .put(12, "甲苯胺蓝染色")
            .put(13, "普鲁氏蓝染色")
            .put(14, "尼氏染色")
            .put(15, "LFB髓鞘染色")
            .put(16, "Tunel染色")
            .put(17, "Ki67")
            /*.put(18, "免疫组织化学染色")
            .put(19, "荧光标记染色")*/
            .put(20, "其他")
            .put(21, "嗜银染色")
            .build();

    /**
     * 颜色类型 - EN
     */
    public static final Map<Integer, String> COLOR_TYPE_EN = new ImmutableMap.Builder<Integer, String>()
            .put(1, "Immunofluorescence")
            .put(2, "Immunohistochemical")
            .put(3, "HE staining")
            .put(4, "Masson staining")
            .put(5, "Van Gieson staining")
            .put(6, "Victoria Blue staining")
            .put(7, "Sudan III/IV")
            .put(8, "Oil Red O")
            .put(9, "PAS")
            .put(10, "AB-PAS")
            .put(11, "Congo red")
            .put(12, "toluidine blue")
            .put(13, "Prussian blue")
            .put(14, "Nissl")
            .put(15, "Luxol Fast Blue myelin")
            .put(16, "Tunel")
            .put(17, "Ki67")
            .put(20, "Other")
            .put(21, "Argyrophilic staining")
            .build();

    /**
     * 眼科-提示语
     */
    public static final Map<Integer, String> EYE_PROMPT_MAP = new ImmutableMap.Builder<Integer, String>()
            .put(1, "算法要求最小图片数量为5张。请删除后重新上传")
            .put(2, "因图像命名不符合要求，未识别到主图，请在原始切片中手动设置主图")
            .put(3, "算法要求最小图片数量为7张。请删除后重新上传")
            .build();


    /**
     * 眼科-提示语-en
     */
    public static final Map<Integer, String> EYE_PROMPT_MAP_EN = new ImmutableMap.Builder<Integer, String>()
            .put(1, "算法要求最小图片数量为5张。请删除后重新上传en")
            .put(2, "因图像命名不符合要求，未识别到主图，请在原始切片中手动设置主图en")
            .put(3, "算法要求最小图片数量为7张。请删除后重新上传en")
            .build();
    /**
     * 眼科ZIP压缩包解压后可解析的图像文件
     */
    public static final ImmutableSet<String> IMAGE_EXT_SET = ImmutableSet.of("png", "jpg");
    
    /**
     * 单审状态
     */
    public static final Map<Integer, String> SELF_REVIEW_STATUS = new ImmutableMap.Builder<Integer, String>()
            .put(1, "未审")
            .put(2, "已审")
            .build();

    /**
     * 单审状态 - EN
     */
    public static final Map<Integer, String> SELF_REVIEW_STATUS_EN = new ImmutableMap.Builder<Integer, String>()
            .put(1, "Unreviewed")
            .put(2, "Reviewed")
            .build();
}
