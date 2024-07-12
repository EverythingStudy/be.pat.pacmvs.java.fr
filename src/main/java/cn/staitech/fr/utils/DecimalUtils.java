package cn.staitech.fr.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author: wangfeng
 * @create: 2024-06-27 14:29:40
 * @Description:
 */

public class DecimalUtils {
    public static String setScale3(BigDecimal bigDecimal) {
        return bigDecimal.setScale(3, RoundingMode.HALF_UP).toString();
    }

    public static String percentScale3(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_UP).toString();
    }

}
