package cn.staitech.fr.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.fr.domain.SingleSlide;

public interface SingleSlideService extends IService<SingleSlide> {

    Boolean forecastResults(Long singleSlideId, Long imageId);
    
    Boolean updateRatTcAreaPerimeter(Long singleSlideId, Long imageId);
}
