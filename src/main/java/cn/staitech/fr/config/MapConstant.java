package cn.staitech.fr.config;

import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.service.OrganizationService;
import cn.staitech.fr.service.StructureService;
import cn.staitech.fr.service.StructureTagService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: MapConstant
 * @Description:
 * @date 2024年4月2日
 */
@Component
public class MapConstant {
    /**
     * 脏器
     */
    public static Map<String, String> CATEGORY_MAP;
    /**
     * 机构
     */
    public static Map<Long, String> ORGANIZATION_MAP;

    public static Map<String, Integer> STRUCTURESIZR_MAP;

    public static Map<String, StructureTag> PATHOLOGICAL_INDICATOR_CATEGORY_MAP = new ConcurrentHashMap<>();

    @Resource
    private OrganService categoryService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private StructureService structureService;

    //    @Resource
//    private PathologicalIndicatorCategoryService pathologicalIndicatorCategoryService;
    @Resource
    private StructureTagService structureTagService;

    /**
     * 获取脏器名称
     *
     * @param
     * @return
     */
    public static String getCategory(String organizationIdCategoryId) {
        if (CATEGORY_MAP.containsKey(organizationIdCategoryId)) {
            return CATEGORY_MAP.get(organizationIdCategoryId);
        }
        return "";
    }

    /**
     * 获取机构名称
     *
     * @param organizationId
     * @return
     */
    public static String getOrganizationName(Long organizationId) {
        if (ORGANIZATION_MAP.containsKey(organizationId)) {

            return ORGANIZATION_MAP.get(organizationId);
        }
        return "";
    }

    /**
     * 获取结构名称
     *
     * @param
     * @return
     */
    public static Integer getStructureSize(String organizationIdStructureId) {
        if (STRUCTURESIZR_MAP.containsKey(organizationIdStructureId)) {
            return STRUCTURESIZR_MAP.get(organizationIdStructureId);
        }
        return null;
    }

    /**
     * 获取结构名称
     *
     * @param structureId
     * @return
     */
    public static StructureTag getPathologicalIndicatorCategory(Long organizationId, String structureId) {
        return PATHOLOGICAL_INDICATOR_CATEGORY_MAP.get(organizationId + structureId);
    }


    @PostConstruct
    public void init() {
        // 脏器
        CATEGORY_MAP = categoryService.getCategory();
        // 机构
        ORGANIZATION_MAP = organizationService.selectMap();
        STRUCTURESIZR_MAP = structureService.selectStructureSizeMap();
        List<StructureTag> pathologicalIndicatorCategories = structureTagService.list(Wrappers.<StructureTag>lambdaQuery().eq(StructureTag::getDelFlag, 0));
        if (CollectionUtils.isNotEmpty(pathologicalIndicatorCategories)) {
            for (StructureTag pathologicalIndicatorCategory : pathologicalIndicatorCategories) {
                if (pathologicalIndicatorCategory.getStructureId() != null) {
                    PATHOLOGICAL_INDICATOR_CATEGORY_MAP.put(pathologicalIndicatorCategory.getOrganizationId() + pathologicalIndicatorCategory.getStructureId(), pathologicalIndicatorCategory);
                }
            }
        }

    }

}
