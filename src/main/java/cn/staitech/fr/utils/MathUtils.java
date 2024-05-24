package cn.staitech.fr.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @Author wudi
 * @Date 2024/5/21 15:00
 * @desc
 */
public class MathUtils {
    public static void main(String[] args) {
        //BigDecimal[] data={new BigDecimal("211"),new BigDecimal("275"),new BigDecimal("334"),new BigDecimal("383"),new BigDecimal("426")};
        BigDecimal[] data = {new BigDecimal("23.6"),new BigDecimal("999999999.213")};

        //保留小数位
        int scale = 1000;
        BigDecimal average = calculateAve(data, scale);
        System.out.println("平均值" + average);

        BigDecimal sum = sum(data, scale);
        System.out.println("Σ(x-x̄)2 / N值" + sum);

        BigDecimal variance = variance(data, scale);
        System.out.println("总体方差" + variance);

        BigDecimal sqrt = sqrt(variance, scale);
        System.out.println("总体标准差" + sqrt);

    }

    /**
     *
     * @param data  原数据集
     * @param scale 保留小数位
     * @return 总体方差
     */
    public static BigDecimal variance(BigDecimal[] data, int scale) {
        if(data.length==1){
            return BigDecimal.ZERO;
        }
        if(data.length<1){
            throw new RuntimeException("数据集的总数应大于0");
        }

        BigDecimal sum = sum(data, scale);
        return sum.divide(new BigDecimal(data.length), new MathContext(scale, RoundingMode.HALF_UP));
        // return  sum .divide(new BigDecimal( data.length),scale,BigDecimal.ROUND_HALF_UP);
    }

    /**
     *
     * @param data  原数据集
     * @param scale 保留小数位
     * @return 样本方差
     */
    public static BigDecimal sampleVariance( BigDecimal[] data, int scale) {
        if(data.length==1){
            return BigDecimal.ZERO;
        }
        if(data.length<1){
            throw new RuntimeException("数据集的总数应大于0");
        }
        BigDecimal sum = sum(data, scale);
        return sum.divide(new BigDecimal(data.length - 1), new MathContext(scale, RoundingMode.HALF_UP));
        //return sum .divide(new BigDecimal( data.length-1),scale,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @param data  数据集
     * @param scale 保留小数
     * @return 总数 Σ(x-x̄)2
     */
    public static BigDecimal sum(BigDecimal[] data, int scale) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            BigDecimal diff = num.subtract(calculateAve(data, scale));
            BigDecimal square = diff.multiply(diff);
            sum = sum.add(square);

        }
        BigDecimal round = sum.round(new MathContext(scale, RoundingMode.HALF_UP));
        //BigDecimal bigDecimal = sum.setScale(scale,BigDecimal.ROUND_HALF_UP);
        return round;
    }

    /**
     * @param data
     * @param scale
     * @return 平均值
     */
    public static BigDecimal calculateAve(BigDecimal[] data, int scale) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            sum = sum.add(num);
        }
        //return sum.divide(new BigDecimal(data.length),scale,BigDecimal.ROUND_HALF_UP);
        return sum.divide(new BigDecimal(data.length), new MathContext(scale, RoundingMode.HALF_UP));
    }

    /**
     * @param data
     * @param scale
     * @return 平均值
     */
    public static BigDecimal calculateAve2(BigDecimal[] data, int scale) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            sum = sum.add(num);
        }

        return sum.divide(new BigDecimal(data.length), scale + 1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @param value
     * @param scale
     * @return 总体标准差
     */
    public static BigDecimal sqrt(BigDecimal value, int scale) {
        if(BigDecimal.ZERO.equals(value)){
            return BigDecimal.ZERO;
        }
        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 1001;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        //deviation = deviation.setScale(scale, BigDecimal.ROUND_HALF_UP);
        deviation = deviation.round(new MathContext(scale, RoundingMode.HALF_UP));

        return deviation;
    }
}
