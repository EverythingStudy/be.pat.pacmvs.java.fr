package cn.staitech.fr.service;

import cn.staitech.fr.domain.AiForecast;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【fr_ai_forecast】的数据库操作Service
* @createDate 2024-04-09 14:42:38
*/
public interface AiForecastService extends IService<AiForecast> {

    Boolean forecastResults(Long singleSlideId, Long imageId);

    void indicatorCount(Long singleSlideId,Long categoryId,String jsonCode);

}
