package cn.staitech.fr.utils;


import org.springframework.stereotype.Component;

import java.text.DecimalFormat;


/**
 * @Author wudi
 * @Date 2024/5/13 16:01
 * @desc
 */
@Component
public class AreaUtils {

    public static String formattedNumber(String res) {
        double value = Double.parseDouble(res);
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(value);
    }




}
