package cn.staitech.fr.utils;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.system.api.domain.SysRole;

import java.util.List;

public class SysRoleUtils {

    public static String getOrganization03Code(long i) {
        String format = String.format("%03d", i);
        String roleSort = "C" + format;
        return roleSort;
    }

    public static String IS_QUALITY_ADMIN = "质量保证人员";
    public static String NUMBER_ADMIN = "数字阅片";
    public static String IS_ORG_ADMIN = "机构管理员";
    public static String INTELL_ADMIN = "智能阅片";
    /**
     * 判断是否为质量保证管理员
     * @return
     */
    public static boolean isQualityAdmin() {
        //拿到质量保证管理员角色
        List<SysRole> roles = SecurityUtils.getRoles();
        boolean isFind = false;
        for (SysRole role : roles) {
            if(null != role.getRoleName()) {
                if(IS_QUALITY_ADMIN.equals(role.getRoleName())) {
                    isFind = true;
                }
            }
        }
        return isFind;
    }

    /**
     * 匹配给定参数的角色信息
     * @return
     */
    public static boolean matchAdmin(String ... roleName) {
        //拿到质量保证管理员角色
        List<SysRole> roles = SecurityUtils.getRoles();
        boolean isFind = false;
        for (SysRole role : roles) {
            if(null != role.getRoleName()) {
                for (String s : roleName) {
                    if(s.equals(role.getRoleName())) {
                        isFind = true;
                        break;
                    }
                }
            }
        }
        return isFind;
    }
}
