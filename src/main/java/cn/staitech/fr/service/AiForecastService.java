package cn.staitech.fr.service;

import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.domain.out.AiForecastListOut;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【fr_ai_forecast】的数据库操作Service
* @createDate 2024-04-09 14:42:38
*/
public interface AiForecastService extends IService<AiForecast> {

    Boolean forecastResults(Long singleSlideId, Long imageId);

    void addAiForecast(Long singleSlideId, Map<String, IndicatorAddIn> indicatorResultsMap);

    void addOutIndicators(Long singleSlideId, Map<String, IndicatorAddIn> indicatorResultsMap);

    List<AiForecast> selectList(Long singleSlideId);


    List<AiForecastListOut> calculateList(Long singleSlideId, String structType);
}
