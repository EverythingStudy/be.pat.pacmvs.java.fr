package cn.staitech.fr.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author wudi
 * @Date 2024/5/21 15:00
 * @desc
 */
@Slf4j
public class MathUtils {
    public static void main(String[] args) {
        //BigDecimal[] data={new BigDecimal("211"),new BigDecimal("275"),new BigDecimal("334"),new BigDecimal("383"),new BigDecimal("426")};
        /*BigDecimal[] data = {new BigDecimal("23.6"),new BigDecimal("999999999.213")};


        //保留小数位
        int scale = 1000;
        BigDecimal average = calculateAve(data, scale);
        System.out.println("平均值" + average);

        BigDecimal sum = sum(data, scale);
        System.out.println("Σ(x-x̄)2 / N值" + sum);

        BigDecimal variance = variance(data, scale);
        System.out.println("总体方差" + variance);

        BigDecimal sqrt = sqrt(variance, scale);
        System.out.println("总体标准差" + sqrt);*/
        //BigDecimal[] data = {new BigDecimal("0.000"),new BigDecimal("0.000")};
        //BigDecimal sd=new BigDecimal("0.000");
        //BigDecimal sd2=new BigDecimal("0.000");
        //System.out.println(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        //System.out.println(sd2.compareTo(BigDecimal.ZERO));
        /*List<BigDecimal> list = new ArrayList<>();
        list.add(new BigDecimal("1.234"));
        String confidenceInterval = getConfidenceInterval(list);
        System.out.println(confidenceInterval);*/
        List<BigDecimal> dataList = new ArrayList<>();
        dataList.add(new BigDecimal("-1"));
        dataList.add(new BigDecimal("0"));
        dataList.add(new BigDecimal("1"));

        if(CollectionUtil.isNotEmpty(dataList)){
            List<BigDecimal> objects = new ArrayList<>(dataList);
            objects.forEach(e->{
                if(e.compareTo(BigDecimal.ZERO)<0){
                    dataList.remove(e);
                }
            });
        }
        System.out.println(dataList);
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
        return sum.divide(new BigDecimal(data.length),scale, RoundingMode.HALF_UP);
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
        return sum.divide(new BigDecimal(data.length), scale, RoundingMode.HALF_UP);
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
        if(BigDecimal.ZERO.compareTo(value)==0){
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
        //deviation = deviation.round(new MathContext(scale, RoundingMode.HALF_UP));
        deviation=
                deviation.setScale(scale, RoundingMode.HALF_UP);
        return deviation;
    }


    /**
     *
     * @param ave 平均值
     * @param deviation 方差
     * @return
     */
    public static String getConfidenceInterval(BigDecimal  ave, BigDecimal deviation){
        //正态分布(下限)
        BigDecimal subtract2 = ave.subtract(new BigDecimal(1.96).multiply(deviation)).setScale(3, RoundingMode.UP);
        //正态分布(上限)
        BigDecimal add2 = ave.add(new BigDecimal(1.96).multiply(deviation)).setScale(3, RoundingMode.UP);
        return subtract2 + "-" + add2;
    }


    /**
     *
     * @param dataList
     * @return 返回指标结果
     */
    public static String getConfidenceInterval(List<BigDecimal> dataList){
        /*List<BigDecimal> objects = new ArrayList<>();
        objects.add(BigDecimal.ZERO);
        //筛掉为零的
        if(CollectionUtil.isNotEmpty(dataList)){
            dataList.removeAll(objects);
        }*/
        if(CollectionUtil.isNotEmpty(dataList)){
            List<BigDecimal> objects = new ArrayList<>(dataList);
            objects.forEach(e->{
                if(e.compareTo(BigDecimal.ZERO)<0){
                    dataList.remove(e);
                }
            });
        }
        if(CollectionUtil.isNotEmpty(dataList)){
            BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
            log.info("平均值"+ bigDecimal);
            BigDecimal variance = MathUtils.variance(dataList.toArray(new BigDecimal[dataList.size()]), 3);
            log.info("总体方差" + variance);
            BigDecimal sqrt = MathUtils.sqrt(variance, 3);
            log.info("总体标准差" + sqrt);

            //正态分布(下限)
            BigDecimal subtract2 = bigDecimal.subtract(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
            if(subtract2.compareTo(BigDecimal.ZERO)<0){
                return bigDecimal+"±"+sqrt;
            }
            //正态分布(上限)
            BigDecimal add2 = bigDecimal.add(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
            return bigDecimal+"±"+sqrt+";"+subtract2 + "-" + add2;
        }else{
            return 0+"±"+0+";"+0 + "-" + 0;
        }

    }

}
