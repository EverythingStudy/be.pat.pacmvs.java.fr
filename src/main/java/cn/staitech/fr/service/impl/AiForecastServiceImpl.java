package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.domain.out.ExportAiListVO;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.AiForecastService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
@Slf4j
public class AiForecastServiceImpl extends ServiceImpl<AiForecastMapper, AiForecast> implements AiForecastService {


    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private SlideMapper slideMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private AiForecastMapper aiForecastMapper;

    @Resource
    private ProjectMapper specialMapper;

    @Override
    public Boolean forecastResults(Long singleSlideId, Long imageId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            SingleSlide singleSlideBy = singleSlideMapper.selectById(singleSlideId);
            if(singleSlideBy == null){
                return false;
            }
            // 查询详情信息
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);
            Category category = categoryMapper.selectById(singleSlideBy.getCategoryId());
            if(category != null && Objects.equals(category.getOrganId(), "08") && category.getSpecies() == 1){
                // 甲状旁腺
                annotation.setCategoryId(singleSlideBy.getCategoryId());
            }
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);

            if (category != null && Objects.equals(category.getOrganId(), "07") && category.getSpecies() == 1) {
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
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
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
            forecast.setResults(indicator.getResult());
            forecast.setUnit(indicator.getUnit());
            forecast.setCreateTime(DateUtil.now());
            if (StringUtils.isNotEmpty(indicator.getStructType())) {
                forecast.setStructType(indicator.getStructType());
            }
            aiForecasts.add(forecast);
        }
        // 批量插入
        if (!CollectionUtils.isEmpty(aiForecasts)) {
            this.saveBatch(aiForecasts);
        }
    }

    /**
     * 新增输出指标
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
                if("0.000".equals(indicator.getResult())){
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

//    @Override
//    public List<AiForecastListOut> calculateList(Long singleSlideId, String structType) {
//        log.info("计算指标列表查询开始：");
//        List<AiForecastListOut> resp = new ArrayList<>();
//        Map<Long, Long> categorys = new HashMap<>();
//        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);
//        Slide slide = slideMapper.selectById(singleSlide.getSlideId());
//        Project special = specialMapper.selectById(slide.getProjectId());
//
//        if (StringUtils.isNotEmpty(special.getControlGroup())) {
//            LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(SingleSlide::getSingleId, singleSlideId);
//            List<SingleSlide> singleSlides = singleSlideMapper.selectList(wrapper);
//            categorys = singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId, SingleSlide::getCategoryId));
//        }
//        LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(AiForecast::getSingleSlideId, singleSlideId);
//        wrapper.eq(AiForecast::getStructType,structType);
//        List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);
//        if(!CollectionUtils.isEmpty(aiForecasts)){
//            for (AiForecast aiForecast : aiForecasts) {
//                AiForecastListOut exportAiListVO = new AiForecastListOut();
//                BeanUtils.copyProperties(aiForecast, exportAiListVO);
//
//
//                if(!CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults())
//                        &&(!aiForecast.getResults().contains("±"))
//                        && new BigDecimal(aiForecast.getResults()).compareTo(BigDecimal.ZERO)<0){
//                    exportAiListVO.setResults("?");
//                }
//                //范围数据
//                if (StringUtils.isNotEmpty(special.getControlGroup())&& !CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults())) {
//
//                    setReferenceScope(special, singleSlideId, exportAiListVO, categorys,slide.getGenderFlag(),structType);
//
//                }
//                resp.add(exportAiListVO);
//            }
//        }
//        return resp;
//    }


//    /**
//     * 设置参考范围
//     * @param special
//     * @param singleId
//     * @param exportAiListVO
//     * @param categorys
//     * @param genderFlag
//     */
//    private void setReferenceScope(Special special, Long singleId, AiForecastListOut exportAiListVO, Map<Long, Long> categorys, String genderFlag,String structType) {
//        List<BigDecimal> dataList= singleSlideMapper.getReferenceScope(exportAiListVO.getQuantitativeIndicators(),categorys.get(singleId), special.getSpecialId(),
//                special.getControlGroup(),genderFlag,structType);
//        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(dataList)) {
//            if (CollectionUtil.isNotEmpty(dataList)) {
//                List<BigDecimal> objects = new ArrayList<>(dataList);
//                objects.forEach(e -> {
//                    if (e.compareTo(BigDecimal.ZERO) < 0) {
//                        dataList.remove(e);
//                    }
//                });
//            }
//            if (CollectionUtil.isNotEmpty(dataList)) {
//                BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
//                log.info("平均值" + bigDecimal);
//                BigDecimal variance = MathUtils.variance(dataList.toArray(new BigDecimal[dataList.size()]), 3);
//                log.info("总体方差" + variance);
//                BigDecimal sqrt = MathUtils.sqrt(variance, 3);
//                log.info("总体标准差" + sqrt);
//                //平均值-标准差
//                //BigDecimal subtract = bigDecimal.subtract(sqrt).setScale(3, RoundingMode.UP);
//                //平均值+标准差
//                //BigDecimal add = bigDecimal.add(sqrt).setScale(3, RoundingMode.UP);
//                exportAiListVO.setAverageValue(bigDecimal + "±" + sqrt);
//
//                //正态分布(下限)
//                BigDecimal subtract2 = bigDecimal.subtract(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
//          /*  if(subtract2.compareTo(BigDecimal.ZERO)<0){
//                subtract2=BigDecimal.ZERO;
//            }*/
//                //正态分布(上限)
//                BigDecimal add2 = bigDecimal.add(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
//                if (subtract2.compareTo(BigDecimal.ZERO) >= 0) {
//                    exportAiListVO.setNormalDistribution(subtract2 + "-" + add2);
//                }
//            }
//
//        }
//
//    }
    private String setRang(Project special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys) {
        if (ObjectUtils.isNotEmpty(categorys.get(singleId))) {
            String rangOut = singleSlideMapper.getRangOut(exportAiListVO.getQuantitativeIndicators(),categorys.get(singleId), special.getProjectId(), special.getControlGroup());
            exportAiListVO.setForecastRange(rangOut);
            return rangOut;
        }
        return null;

    }


}




