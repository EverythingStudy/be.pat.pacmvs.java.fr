package cn.staitech.fr.utils;

public class SysRoleUtils {

    public static String getOrganization03Code(long i) {
        String format = String.format("%03d", i);
        String roleSort = "C" + format;
        return roleSort;
    }

}
