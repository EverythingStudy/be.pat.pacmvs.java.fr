package cn.staitech.fr.utils;

import java.text.NumberFormat;

public class OrganizationUtils {

    public static String geNumber(Long organizationId){
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(3);
        formatter.setGroupingUsed(false);
        return "C" + formatter.format(organizationId);
    }
}
