package cn.staitech.fr.config;

import org.springframework.stereotype.Component;

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
     * 机构
     */
    public static Map<Long, String> ORGANIZATION_MAP;


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
}
