package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.config.OrganStructureConfig;
import cn.staitech.fr.config.TraceContext;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.domain.out.AiForecastListOut;
import cn.staitech.fr.domain.out.ExportAiListVO;
import cn.staitech.fr.enums.JsonTaskStatusEnum;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.JsonTaskService;
import cn.staitech.fr.service.strategy.json.*;
import cn.staitech.fr.utils.MathUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.gson.JsonObject;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
@Slf4j
public class AiForecastServiceImpl extends ServiceImpl<AiForecastMapper, AiForecast> implements AiForecastService {

    Executor executor = new ThreadPoolExecutor(2, 20, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());
    // 包装线程池
    Executor ttlExecutor = TtlExecutors.getTtlExecutor(executor);
    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private SlideMapper slideMapper;

    @Resource
    private AiForecastMapper aiForecastMapper;

    @Resource
    private ProjectMapper specialMapper;
    @Resource
    private OrganTagMapper organTagMapper;
    @Resource
    private JsonTaskService jsonTaskService;
    @Resource
    @Lazy
    private JsonTaskParserService jsonTaskParserService;
    @Autowired
    private JsonFileMapper jsonFileMapper;

    @Override
    public Boolean forecastResults(Long singleSlideId, Long imageId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            SingleSlide singleSlideBy = singleSlideMapper.selectById(singleSlideId);
            if (singleSlideBy == null) {
                return false;
            }
            // 查询详情信息
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);

            OrganTag category = organTagMapper.selectById(singleSlideBy.getCategoryId());
            if (category != null && Objects.equals(category.getOrganTagCode(), "08") && category.getSpeciesId() == "1") {
                // 甲状旁腺
                annotation.setCategoryId(singleSlideBy.getCategoryId());
            }
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);

            if (category != null && Objects.equals(category.getOrganTagCode(), "07") && category.getSpeciesId() == "1") {
                // 甲状腺
                annotationBy = annotationMapper.unionGeometryArea(singleSlideId);
            }
            if (annotationBy == null || annotationBy.getArea() == null) {
                return false;
            }
            // 查询图像分辨率
            Image image = imageMapper.selectById(imageId);
            if (image == null) {
                return false;
            }
            if (!Optional.ofNullable(image.getResolutionX()).isPresent()) {
                return false;
            }
            double resolutions = Double.parseDouble(image.getResolutionX());
            double areas = (Double.parseDouble(annotationBy.getArea()) * resolutions * resolutions) * 0.000001;
            BigDecimal bd1 = new BigDecimal(Double.toString(areas));
            bd1 = bd1.setScale(9, RoundingMode.HALF_UP);
            String area = bd1.toPlainString();
            double perimeters = (Double.parseDouble(annotationBy.getPerimeter()) * resolutions) * 0.001;
            BigDecimal bd = new BigDecimal(Double.toString(perimeters));
            bd = bd.setScale(9, RoundingMode.HALF_UP);
            String perimeter = bd.toPlainString();
            annotationBy.setArea(area);
            SingleSlide singleSlide = new SingleSlide();
            singleSlide.setSingleId(singleSlideId);
            singleSlide.setArea(annotationBy.getArea());
            singleSlide.setPerimeter(perimeter);
            int res = singleSlideMapper.updateById(singleSlide);
            if (res > 0) {
                JsonTask jsonTask = jsonTaskService.getOne(new LambdaQueryWrapper<>(JsonTask.class).eq(JsonTask::getSingleId, singleSlideId));
                if (!Objects.isNull(jsonTask) && JsonTaskStatusEnum.PARSE_NOT_START.getCode().equals(jsonTask.getStatus())) {
                    Date startTime = new Date();
                    log.info("jsonTask id:{} singleSlide id:{} checkJson 精细轮廓进入指标开始 startTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.formatDateTime(startTime));
                    List<JsonFile> fileList = jsonFileMapper.selectList(Wrappers.<JsonFile>lambdaQuery().eq(JsonFile::getTaskId, jsonTask.getTaskId()).eq(JsonFile::getAiStatus, 0).isNotNull(JsonFile::getFileUrl));
                    ttlExecutor.execute(Objects.requireNonNull(TtlRunnable.get(() -> {
                        jsonTaskParserService.structureFileCalculate(jsonTask, fileList);
                    })));
                    log.info("jsonTask id:{} singleSlide id:{} checkJson 精细轮廓进入指标结束 endTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.between(startTime, new Date(), DateUnit.SECOND));
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("forecastResults异常:{}", ex.getMessage());
            return false;
        }
    }

    /**
     * 批量插入指标预测结果
     */
    @Override
    public void addAiForecast(Long singleSlideId, Map<String, IndicatorAddIn> indicatorResultsMap) {
        List<AiForecast> aiForecasts = new ArrayList<>();
        for (Map.Entry<String, IndicatorAddIn> entry : indicatorResultsMap.entrySet()) {
            // 指标名称
            String indicatorCode = entry.getKey();
            // 指标信息
            IndicatorAddIn indicator = entry.getValue();
            AiForecast forecast = new AiForecast();
            forecast.setSingleSlideId(singleSlideId);
            forecast.setQuantitativeIndicators(indicatorCode);
            forecast.setQuantitativeIndicatorsEn(indicator.getEnglishName());
            // 默认indicator.getResult()
            forecast.setResults(indicator.getResult());
            // 如果是：均值±标准差@分隔追加元数据文件地址
            if (indicator.getResult() != null) {
                int atIndex = indicator.getResult().indexOf('@');
                if (atIndex != -1) {
                    // 截取 @ 之前
                    String before = indicator.getResult().substring(0, atIndex);
                    // 截取 @ 之后
                    String after = indicator.getResult().substring(atIndex + 1);
                    forecast.setResults(before);
                    forecast.setFileUrl(after);
                }
            }

            forecast.setUnit(indicator.getUnit());
            forecast.setCreateTime(DateUtil.now());
            if (StringUtils.isNotEmpty(indicator.getStructType())) {
                forecast.setStructType(indicator.getStructType());
            }
            forecast.setStructureIds(indicator.getStructureIds());
            aiForecasts.add(forecast);
        }
        // 批量插入
        if (!CollectionUtils.isEmpty(aiForecasts)) {
            this.saveBatch(aiForecasts);
        }
    }

    /**
     * 新增输出指标
     *
     * @param singleSlideId
     * @param indicatorResultsMap
     */
    @Override
    public void addOutIndicators(Long singleSlideId, Map<String, IndicatorAddIn> indicatorResultsMap) {
        List<AiForecast> aiForecasts = new ArrayList<>();
        for (Map.Entry<String, IndicatorAddIn> entry : indicatorResultsMap.entrySet()) {
            // 指标名称
            String indicatorCode = entry.getKey();
            // 指标信息
            IndicatorAddIn indicator = entry.getValue();

            AiForecast forecast = new AiForecast();
            forecast.setSingleSlideId(singleSlideId);
            forecast.setQuantitativeIndicators(indicatorCode);
            forecast.setQuantitativeIndicatorsEn(indicator.getEnglishName());
            forecast.setResults(indicator.getResult());
            forecast.setUnit(indicator.getUnit());
            forecast.setCreateTime(DateUtil.now());

            if (StringUtils.isNotEmpty(indicator.getStructType())) {
                if ("0.000".equals(indicator.getResult())) {
                    continue;
                }
                forecast.setStructType(indicator.getStructType());
            }

            aiForecasts.add(forecast);
        }
        // 批量插入
        if (!CollectionUtils.isEmpty(aiForecasts)) {
            this.saveBatch(aiForecasts);
        }
    }


    @Override
    public List<AiForecast> selectList(Long singleSlideId) {
        Map<Long, Long> categorys = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);
        Slide slide = slideMapper.selectById(singleSlide.getSlideId());
        Project special = specialMapper.selectById(slide.getProjectId());

        if (StringUtils.isNotEmpty(special.getControlGroup())) {
            LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SingleSlide::getSingleId, singleSlideId);
            List<SingleSlide> singleSlides = singleSlideMapper.selectList(wrapper);
            categorys = singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId, SingleSlide::getCategoryId));
        }
        LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiForecast::getSingleSlideId, singleSlideId);
        List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(aiForecasts)) {
            for (AiForecast aiForecast : aiForecasts) {
                ExportAiListVO exportAiListVO = new ExportAiListVO();
                BeanUtils.copyProperties(aiForecast, exportAiListVO);
                //范围数据
                if (StringUtils.isNotEmpty(special.getControlGroup())) {
                    String result = setRang(special, singleSlideId, exportAiListVO, categorys);
                    aiForecast.setResults(result);
                }
            }
        }
        return aiForecasts;
    }

    @Override
    public List<AiForecastListOut> calculateList(Long singleSlideId, String structType) {
        log.info("计算指标列表查询开始：");
        List<AiForecastListOut> resp = new ArrayList<>();
        Map<Long, Long> categorys = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);
        Slide slide = slideMapper.selectById(singleSlide.getSlideId());
        Project special = specialMapper.selectById(slide.getProjectId());

        if (StringUtils.isNotEmpty(special.getControlGroup())) {
            LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SingleSlide::getSingleId, singleSlideId);
            List<SingleSlide> singleSlides = singleSlideMapper.selectList(wrapper);
            categorys = singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId, SingleSlide::getCategoryId));
        }
        LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiForecast::getSingleSlideId, singleSlideId);
        wrapper.eq(AiForecast::getStructType, structType);
        List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(aiForecasts)) {
            for (AiForecast aiForecast : aiForecasts) {
                AiForecastListOut exportAiListVO = new AiForecastListOut();
                BeanUtils.copyProperties(aiForecast, exportAiListVO);
                if (!CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults()) && (!aiForecast.getResults().contains("±")) && new BigDecimal(aiForecast.getResults()).compareTo(BigDecimal.ZERO) < 0) {
                    exportAiListVO.setResults("?");
                }
                //范围数据
                if (StringUtils.isNotEmpty(special.getControlGroup()) && !CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults())) {
                    setReferenceScope(special, singleSlideId, exportAiListVO, categorys, slide.getGenderFlag(), structType);
                }
                resp.add(exportAiListVO);
            }
        }
        return resp;
    }


    /**
     * 设置参考范围
     *
     * @param special
     * @param singleId
     * @param exportAiListVO
     * @param categorys
     * @param genderFlag
     */
    private void setReferenceScope(Project special, Long singleId, AiForecastListOut exportAiListVO, Map<Long, Long> categorys, String genderFlag, String structType) {
        List<BigDecimal> dataList = singleSlideMapper.getReferenceScope(exportAiListVO.getQuantitativeIndicators(), categorys.get(singleId), special.getProjectId(), special.getControlGroup(), genderFlag, structType);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dataList)) {
            if (CollectionUtil.isNotEmpty(dataList)) {
                List<BigDecimal> objects = new ArrayList<>(dataList);
                objects.forEach(e -> {
                    if (e.compareTo(BigDecimal.ZERO) < 0) {
                        dataList.remove(e);
                    }
                });
            }
            if (CollectionUtil.isNotEmpty(dataList)) {
                BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
                log.info("平均值" + bigDecimal);
                BigDecimal variance = MathUtils.variance(bigDecimal,dataList.toArray(new BigDecimal[dataList.size()]), 3);
                log.info("总体方差" + variance);
                BigDecimal sqrt = MathUtils.sqrt(variance, 3);
                log.info("总体标准差" + sqrt);
                //平均值-标准差
                //BigDecimal subtract = bigDecimal.subtract(sqrt).setScale(3, RoundingMode.UP);
                //平均值+标准差
                //BigDecimal add = bigDecimal.add(sqrt).setScale(3, RoundingMode.UP);
                exportAiListVO.setAverageValue(bigDecimal + "±" + sqrt);

                //正态分布(下限)
                BigDecimal subtract2 = bigDecimal.subtract(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
          /*  if(subtract2.compareTo(BigDecimal.ZERO)<0){
                subtract2=BigDecimal.ZERO;
            }*/
                //正态分布(上限)
                BigDecimal add2 = bigDecimal.add(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
                if (subtract2.compareTo(BigDecimal.ZERO) >= 0) {
                    exportAiListVO.setNormalDistribution(subtract2 + "-" + add2);
                }
            }

        }

    }

    private String setRang(Project special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys) {
        if (ObjectUtils.isNotEmpty(categorys.get(singleId))) {
            String rangOut = singleSlideMapper.getRangOut(exportAiListVO.getQuantitativeIndicators(), categorys.get(singleId), special.getProjectId(), special.getControlGroup());
            exportAiListVO.setForecastRange(rangOut);
            return rangOut;
        }
        return null;

    }
}




