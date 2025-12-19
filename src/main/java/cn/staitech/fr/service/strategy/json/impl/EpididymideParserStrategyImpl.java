package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: EpididymideParserStrategyImpl
 * @Description-d:附睾
 * @date 2025年7月22日
 */
@Slf4j
@Service("Epididymide")
public class EpididymideParserStrategyImpl extends AbstractCustomParserStrategy {
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource(name = "dynamicDataThreadPool")
    private ExecutorService dynamicDataThreadPool;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("EpididymideParserStrategyImpl init");
    }


    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        // 获取各种指标
        // B 输出小管/附睾管黏膜上皮面积（全片）mm2
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "12F0F5").getStructureAreaNum();
        // E 输出小管/附睾管管腔面积（全片）mm2
        BigDecimal organAreaE = commonJsonParser.getOrganArea(jsonTask, "12F0F4").getStructureAreaNum();
        // G 精子面积（全片）
        BigDecimal organAreaG = commonJsonParser.getOrganArea(jsonTask, "12F0F7").getStructureAreaNum();
        // I 血管面积
        BigDecimal organAreaI = commonJsonParser.getOrganArea(jsonTask, "12F003").getStructureAreaNum();
        // J 组织轮廓面积
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        BigDecimal organAreaJ = new BigDecimal(singleSlide.getArea());
        // C 输出小管/附睾管黏膜上皮周长（单个）mm
        Annotation annotation1 = new Annotation();
        annotation1.setPerimeterName("输出小管/附睾管黏膜上皮周长（单个）");
        annotation1.setPerimeterUnit(MM);
        Date startTime1 = new Date();
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12F0F5", annotation1, 3);
        // D 输出小管/附睾管管腔面积（单个）103 μm2
        Annotation annotationBy1 = new Annotation();
        annotationBy1.setAreaName("输出小管/附睾管管腔面积（单个）");
        annotationBy1.setAreaUnit(SQ_UM_THOUSAND);
        Date startTime3 = new Date();
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12F0F5", "12F0F4", annotationBy1, 1,true);
        log.info("jsonTask id:{} singleSlide id:{} 输出小管/附睾管黏膜上皮周长（单个）endTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.between(startTime1, new Date(), DateUnit.SECOND));
        //F 精子面积（单个）103 μm2
        Annotation annotationBy2 = new Annotation();
        annotationBy2.setAreaName("精子面积（单个）");
        annotationBy2.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12F0F7", annotationBy2, 1);
        // H 黏膜上皮细胞核数量（单个) 个
        Annotation annotation2s = new Annotation();
        annotation2s.setCountName("黏膜上皮细胞核数量（单个）");
        Date startTime2 = new Date();
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12F0F5", "12F0F6", annotation2s);
        log.info("jsonTask id:{} singleSlide id:{} 黏膜上皮细胞核数量（单个）endTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.between(startTime2, new Date(), DateUnit.SECOND));
        // K 输出小管/附睾管黏膜上皮面积（单个）103 μm2
        Annotation annotationBy = new Annotation();
        annotationBy.setAreaName("输出小管/附睾管黏膜上皮面积（单个）");
        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
        Date startTime = new Date();
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12F0F5","12F0F4", annotationBy, 1,false);
        log.info("jsonTask id:{} singleSlide id:{} 输出小管/附睾管黏膜上皮面积（单个） endTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.between(startTime, new Date(), DateUnit.SECOND));
        Map<String, IndicatorAddIn> resultsMap = new HashMap<>();
        // 算法输出指标
        resultsMap.put("输出小管/附睾管管腔面积（全片）", createIndicator(organAreaE, SQ_MM, areaUtils.getStructureIds("12F0F5", "12F0F4")));
        resultsMap.put("精子面积（全片）", createIndicator(organAreaG, SQ_MM, areaUtils.getStructureIds("12F0F5", "12F0F7")));
        resultsMap.put("血管面积", createIndicator(organAreaI.setScale(3, RoundingMode.HALF_UP), SQ_MM, "12F003"));

        // 产品呈现指标
        BigDecimal one = new BigDecimal("1");

        // 4 黏膜上皮面积占比（单个）4=1-D/A
        List<BigDecimal> list1 = new ArrayList<>();
        //A 输出小管/附睾管黏膜上皮外轮廓面积
        List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask, "12F0F5");
        // 使用线程池并行处理
        List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();
        for (Annotation i : annotationList1) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                //D 输出小管/附睾管管腔面积（单个）
                BigDecimal areaNum = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F4", true).getStructureAreaNum();
                return one.subtract(getProportion(areaNum, i.getStructureAreaNum()));
            }, dynamicDataThreadPool);
            futures.add(future);
        }
        // 等待所有任务完成并收集结果
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.join();
        } catch (Exception e) {
            log.error("并行处理注解数据失败", e);
        }
        // 收集处理结果并批量更新
        list1 = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        futures.clear();
        String mucosalAreaPer = MathUtils.getConfidenceInterval(list1);

        // 5 精子面积占比（单个）5=F/D
        List<BigDecimal> list2 = new ArrayList<>();
        List<String> list3s = new ArrayList<>();
//        List<String> list4s = new ArrayList<>();
        //D 输出小管/附睾管管腔面积（单个）103 μm2
        List<Annotation> annotationList2 = commonJsonParser.getStructureContourList(jsonTask, "12F0F5");
        for (Annotation i : annotationList2) {
            //F 精子面积（单个）103 μm2
            Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F7", true);
            Annotation annotation3 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F4", true);
            BigDecimal res = getProportion(new BigDecimal(areaUtils.micrometerToSquareMicrometer(annotation2.getArea())), new BigDecimal(areaUtils.micrometerToSquareMicrometer(annotation3.getArea())));
            list3s.add(areaUtils.micrometerToSquareMicrometer(annotation2.getArea()));
            //list4s.add(areaUtils.micrometerToSquareMicrometer(i.getArea()));
            list2.add(res);
        }

        String spermAreaPer = MathUtils.getConfidenceInterval(list2);

        // 7 黏膜上皮细胞核密度（单个）7=H/C
        List<BigDecimal> list3 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                //H 黏膜上皮细胞核数量 个
                Integer count = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F6", true).getCount();
                return commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(count), i.getStructurePerimeterNum());
            }, dynamicDataThreadPool);
            futures.add(future);
        }
        // 等待所有任务完成并收集结果
        CompletableFuture<Void> allFuture3 = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFuture3.join();
        } catch (Exception e) {
            log.error("并行处理注解数据失败", e);
        }
        // 收集处理结果并批量更新
        list3 = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        futures.clear();
        String mucosalCellDensity = MathUtils.getConfidenceInterval(list3);

        //9 黏膜上皮厚度（单个）
        List<BigDecimal> list4 = new ArrayList<>();
        for (Annotation i : annotationList1) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                Annotation annotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask, i.getContour(), "12F0F4", true);
                if (i.getArea() != null && annotation2.getArea() != null) {
                    BigDecimal sqrtI = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(i.getArea()), new BigDecimal(A));
                    BigDecimal sqrt1 = commonJsonParser.sqrt(sqrtI);
                    BigDecimal sqrtAnnotation = commonJsonParser.bigDecimalDivideCheck(new BigDecimal(annotation2.getArea()), new BigDecimal(A));
                    BigDecimal sqrt2 = commonJsonParser.sqrt(sqrtAnnotation);
                    return sqrt1.subtract(sqrt2);
                }
                return null;
            }, dynamicDataThreadPool);
            futures.add(future);
        }
        // 等待所有任务完成并收集结果
        CompletableFuture<Void> allFuture4 = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFuture4.join();
        } catch (Exception e) {
            log.error("并行处理注解数据失败", e);
        }
        // 收集处理结果并批量更新
        list4 = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        futures.clear();
        String mucosalThickness = MathUtils.getConfidenceInterval(list4);
        //1 附睾面积 mm2 1=J
        resultsMap.put("附睾面积", createNameIndicator("Epididymal area", organAreaJ.setScale(3, RoundingMode.HALF_UP), SQ_MM, "12F111"));
        //2 输出小管和附睾管面积占比（全片） % 2=B/J
        resultsMap.put("输出小管和附睾管面积占比（全片）", createNameIndicator("Efferent ducts and epididymal ducts area%（all）", getProportion(organAreaB, organAreaJ), PERCENTAGE, areaUtils.getStructureIds("12F0F5", "12F111")));
        //3 间质面积占比 % 1-B/J
        resultsMap.put("间质面积占比", createNameIndicator("Mesenchyme area%", one.subtract(getProportion(organAreaB, organAreaJ)), PERCENTAGE, areaUtils.getStructureIds("12F0F5", "12F111")));
        //4 黏膜上皮面积占比 % 4=1-D/A
        resultsMap.put("黏膜上皮面积占比（单个）", createNameIndicator("Mucosal epithelium area% (per)", mucosalAreaPer, PERCENTAGE, areaUtils.getStructureIds("12F0F5", "12F0F4")));
        //5 精子面积占比 % 5=F/D
        resultsMap.put("精子面积占比（单个）", createNameIndicator("Sperm area% (per)", spermAreaPer, PERCENTAGE, areaUtils.getStructureIds("12F0F5", "12F0F7", "12F0F4")));
        //6 精子面积占比 % 6=G/E
        resultsMap.put("精子面积占比（全片）", createNameIndicator("Sperm area% (all)", getProportion(organAreaG, organAreaE), PERCENTAGE, areaUtils.getStructureIds("12F0F5", "12F0F7", "12F0F4")));
        //7 黏膜上皮细胞核密度 % 7=H/C
        resultsMap.put("黏膜上皮细胞核密度（单个）", createNameIndicator("Mucosal epithelial nucleus% (per)", mucosalCellDensity, MM_PIECE, areaUtils.getStructureIds("12F0F6", "12F0F5")));
        //8 血管面积占比 % 8=I/J
        resultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(organAreaI, organAreaJ), PERCENTAGE, areaUtils.getStructureIds("12F003", "12F111")));
        //9 黏膜上皮厚度 μm 9=\sqrt{\smash[b]{A/\pi}}-\sqrt{\smash[b]{D/\pi}}
        resultsMap.put("黏膜上皮厚度（单个）", createNameIndicator("Average thickness of mucosal epithelium (per)", mucosalThickness, UM, areaUtils.getStructureIds("12F0F5", "12F0F4")));
        aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
    }

    @Override
    public String getAlgorithmCode() {
        return "Epididymide";
    }
}
