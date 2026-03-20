package cn.staitech.fr.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/5/21 15:00
 * @desc
 */
@Slf4j
public class MathUtils {
    public static void main(String[] args) {
        BigDecimal[] data = {new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426"), new BigDecimal("211"), new BigDecimal("275"), new BigDecimal("334"), new BigDecimal("383"), new BigDecimal("426")};
        //String string = getFirstAndLastOfMiddle95Percent(Arrays.asList(data).stream().sorted().collect(Collectors.toList()));
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

        if (CollectionUtil.isNotEmpty(dataList)) {
            List<BigDecimal> objects = new ArrayList<>(dataList);
            objects.forEach(e -> {
                if (e.compareTo(BigDecimal.ZERO) < 0) {
                    dataList.remove(e);
                }
            });
        }
        System.out.println(dataList);
    }

    /**
     * @param data  原数据集
     * @param scale 保留小数位
     * @return 总体方差
     */
    public static BigDecimal variance(BigDecimal[] data, int scale) {
        if (data.length == 1) {
            return BigDecimal.ZERO;
        }
        if (data.length < 1) {
            throw new RuntimeException("数据集的总数应大于0");
        }

        BigDecimal sum = sum(data, scale);
        return sum.divide(new BigDecimal(data.length), scale, RoundingMode.HALF_UP);
        // return  sum .divide(new BigDecimal( data.length),scale,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @param data  原数据集
     * @param scale 保留小数位
     * @return 样本方差
     */
    public static BigDecimal sampleVariance(BigDecimal[] data, int scale) {
        if (data.length == 1) {
            return BigDecimal.ZERO;
        }
        if (data.length < 1) {
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
        if (BigDecimal.ZERO.compareTo(value) == 0) {
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
        deviation = deviation.setScale(scale, RoundingMode.HALF_UP);
        return deviation;
    }


    /**
     * @param ave       平均值
     * @param deviation 方差
     * @return
     */
    public static String getConfidenceInterval(BigDecimal ave, BigDecimal deviation) {
        //正态分布(下限)
        BigDecimal subtract2 = ave.subtract(new BigDecimal(1.96).multiply(deviation)).setScale(3, RoundingMode.UP);
        //正态分布(上限)
        BigDecimal add2 = ave.add(new BigDecimal(1.96).multiply(deviation)).setScale(3, RoundingMode.UP);
        return subtract2 + "-" + add2;
    }


    /**
     * 均值±标准差；中间95%数据分布区间
     *
     * @param dataList
     * @return 返回指标结果
     */
    public static String getConfidenceInterval(List<BigDecimal> dataList) {
        if (CollectionUtil.isNotEmpty(dataList)) {
            List<BigDecimal> objects = new ArrayList<>(dataList);
            objects.forEach(e -> {
                if (e.compareTo(BigDecimal.ZERO) < 0) {
                    dataList.remove(e);
                }
            });
        }
        if (CollectionUtil.isNotEmpty(dataList)) {
            BigDecimal average = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
            BigDecimal variance = MathUtils.variance(dataList.toArray(new BigDecimal[dataList.size()]), 3);
            BigDecimal sqrt = MathUtils.sqrt(variance, 3);
            return average + "±" + sqrt;
            // V3.6.4去掉95区间
          /*  String middle95Percent = getFirstAndLastOfMiddle95Percent(dataList.stream().sorted().map(e -> e.setScale(3, RoundingMode.UP)).collect(Collectors.toList()), dataList.size());
            return bigDecimal + "±" + sqrt + ";" + middle95Percent;*/
        } else {
            return 0 + "±" + 0;
        }
    }

    /**
     * @param decimalList
     * @param count       对应脏器对照组切片数量
     * @param
     * @return
     */
    public static String getFirstAndLastOfMiddle95Percent(List<BigDecimal> decimalList, Integer count) {
        List<BigDecimal> dataList = decimalList.stream().sorted().map(e -> e.setScale(3, RoundingMode.UP)).collect(Collectors.toList());
        //数量[1,5)
        if (count == null || count < 5) {
            return "数据量过少,无统计学意义";
        }
        if (dataList.size() == 0) {
            return "0-0";
        }
        if (count < 40) {
            BigDecimal firstNumber = dataList.get(1);
            BigDecimal lastNumber = dataList.get(dataList.size() - 2);
            if (getCompTO(firstNumber, lastNumber) <= 0) {
                return firstNumber + "-" + lastNumber;
            } else {
                return lastNumber + "-" + firstNumber;
            }
            //        	return dataList.get(1) + "-" + dataList.get(dataList.size() - 2);
        }
        // 计算前 2.5% 和后 2.5% 的数据量
        int totalSize = dataList.size();
        int removeCount = (int) Math.ceil(totalSize * 0.025);
        // 计算起始和结束索引
        int startIndex = removeCount;
        int endIndex = totalSize - removeCount;
        // 确保剩余数据量至少为 1
        if (totalSize - 2 * removeCount < 1) {
            //log.error("剩余数量小于1");
            BigDecimal firstNumber = dataList.get(startIndex - 1);
            BigDecimal lastNumber = dataList.get(endIndex - 1);
            if (getCompTO(firstNumber, lastNumber) <= 0) {
                return firstNumber + "-" + lastNumber;
            } else {
                return lastNumber + "-" + firstNumber;
            }
            //return dataList.get(startIndex - 1) + "-" + dataList.get(endIndex - 1);
        }
        // 截取中间 95% 的数据
        //List<T> middleSubList = dataList.subList(startIndex, endIndex);

        // 获取第一个数和最后一个数
        BigDecimal firstNumber = dataList.get(startIndex - 1);
        BigDecimal lastNumber = dataList.get(endIndex - 1);
        if (getCompTO(firstNumber, lastNumber) <= 0) {
            return firstNumber + "-" + lastNumber;
        } else {
            return lastNumber + "-" + firstNumber;
        }
        //return firstNumber + "-" + lastNumber;
    }

    public static int getCompTO(BigDecimal startValue, BigDecimal endValue) {
        int result = startValue.compareTo(endValue);
        return result;
    }


}
