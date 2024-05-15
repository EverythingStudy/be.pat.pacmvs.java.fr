package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.mapper.AiForecastMapper;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

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
            annotationBy.setArea(area);
            SingleSlide singleSlide = new SingleSlide();
            singleSlide.setSingleId(singleSlideId);
            singleSlide.setArea(annotationBy.getArea());
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
            aiForecasts.add(forecast);
        }
        // 批量插入
        if (!CollectionUtils.isEmpty(aiForecasts)) {
            this.saveBatch(aiForecasts);
        }
    }

}




