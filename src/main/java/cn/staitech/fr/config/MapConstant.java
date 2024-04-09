package cn.staitech.fr.config;

import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.service.OrganizationService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

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


    @Resource
    private CategoryService categoryService;
    @Resource
    private OrganizationService organizationService;

    /**
     * 获取脏器名称
     *
     * @param getOrgan
     * @return
     */
    public static String getCategory(Long organizationIdCategoryId) {
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

    @PostConstruct
    public void init() {
        // 脏器
        CATEGORY_MAP = categoryService.getCategory();
        // 机构
        ORGANIZATION_MAP = organizationService.selectMap();
    }

}
