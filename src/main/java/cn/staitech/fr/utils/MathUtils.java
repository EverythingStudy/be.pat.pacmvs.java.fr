package cn.staitech.fr.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
     * 总体方差
     *
     * @param average 平均值
     * @param data    原数据集
     * @param scale   保留小数位
     * @return 总体方差
     */
    public static BigDecimal variance(BigDecimal average, BigDecimal[] data, int scale) {
        if (data.length == 1) {
            return BigDecimal.ZERO;
        }
        if (data.length < 1) {
            throw new RuntimeException("数据集的总数应大于0");
        }

        BigDecimal sum = sum(average, data, scale);
        return sum.divide(new BigDecimal(data.length), scale, RoundingMode.HALF_UP);
    }

    /**
     * @param average 平均值
     * @param data    数据集
     * @param scale   保留小数
     * @return 总数 Σ(x-x̄)2
     */
    public static BigDecimal sum(BigDecimal average, BigDecimal[] data, int scale) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            BigDecimal diff = num.subtract(average);
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
     * @return 返回指标结果：@分隔追加元数据文件地址
     */
    public static String getConfidenceInterval(List<BigDecimal> dataList) {
        log.info("开始计算均值±标准差,元素数量：{}", dataList.size());
        if (CollectionUtil.isNotEmpty(dataList)) {
            List<BigDecimal> objects = new ArrayList<>(dataList);
            objects.forEach(e -> {
                if (e.compareTo(BigDecimal.ZERO) < 0) {
                    dataList.remove(e);
                }
            });
        }
        if (CollectionUtil.isNotEmpty(dataList)) {
            // 元数据存放到文件
            String url = MDC.get(CommonConstant.FORECAST_FILE_URL_MDC) + UUID.randomUUID() + "-" + dataList.size() + ".txt";
            saveForecastFile(url, dataList);
            log.info("开始计算均值");
            BigDecimal average = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
            log.info("均值为:{}", average);

            log.info("开始计算方差");
            BigDecimal variance = MathUtils.variance(average, dataList.toArray(new BigDecimal[dataList.size()]), 3);
            log.info("方差为:{}", variance);

            log.info("开始计算标准差");
            BigDecimal sqrt = MathUtils.sqrt(variance, 3);
            log.info("标准差为:{}", sqrt);
            return average + "±" + sqrt + "@" + url;
            // V3.6.4去掉95区间
          /*  String middle95Percent = getFirstAndLastOfMiddle95Percent(dataList.stream().sorted().map(e -> e.setScale(3, RoundingMode.UP)).collect(Collectors.toList()), dataList.size());
            return bigDecimal + "±" + sqrt + ";" + middle95Percent;*/
        } else {
            return 0 + "±" + 0;
        }
    }

    /**
     * 保存指标元数据到文件
     *
     * @param url      文件地址
     * @param dataList 元数据
     */
    private static void saveForecastFile(String url, List<BigDecimal> dataList) {
        log.info("保存指标元数据到文件开始，文件地址：{}", url);
        File file = new File(url);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.error("无法创建目录: {}", parentDir.getAbsolutePath());
                return;
            }
        }
        // 使用 try-with-resources 自动关闭流，防止资源泄露
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(url))) {
            int count = 0;
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < dataList.size(); i++) {
                BigDecimal value = dataList.get(i);
                // 处理空值情况，避免空指针，视业务需求也可选择跳过或写 "null"
                String valStr = (value != null) ? value.toPlainString() : "";
                lineBuilder.append(valStr);
                count++;
                // 每满 100 个，或者到达列表末尾时，进行换行写入
                if (count % 100 == 0 || i == dataList.size() - 1) {
                    writer.write(lineBuilder.toString());
                    // 写入平台相关的换行符
                    writer.newLine();
                    // 清空 StringBuilder 以复用
                    lineBuilder.setLength(0);
                } else {
                    // 如果不是该行最后一个，添加逗号
                    lineBuilder.append(",");
                }
            }
        } catch (Exception e) {
            log.error("保存指标元数据到文件异常：", e);
        }
        log.info("保存指标元数据到文件结束，文件地址：{}", url);
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
