package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.domain.out.*;
import cn.staitech.fr.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.AiForecastService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
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
    private AiForecastMapper aiForecastMapper;

    @Resource
    private SpecialMapper specialMapper;

    @Override
    public Boolean forecastResults(Long singleSlideId, Long imageId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);
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
            String area = String.format("%.3f", areas);
            double perimeters = (Double.parseDouble(annotationBy.getArea()) * resolutions * resolutions) * 0.000001;
            String perimeter = String.format("%.3f", perimeters);
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
            if(StringUtils.isNotEmpty(indicator.getStruct_type())){
                forecast.setStructType(indicator.getStruct_type());
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
        Special special = specialMapper.selectById(slide.getSpecialId());
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

    private String setRang(Special special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys) {
        if (ObjectUtils.isNotEmpty(categorys.get(singleId))) {
            String rangOut = singleSlideMapper.getRangOut(categorys.get(singleId), special.getSpecialId(), special.getControlGroup());
            exportAiListVO.setForecastRange(rangOut);
            return rangOut;
        }
        return null;

    }


}




