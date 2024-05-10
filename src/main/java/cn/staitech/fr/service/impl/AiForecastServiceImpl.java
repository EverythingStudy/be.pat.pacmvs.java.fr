package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.mapper.AnnotationMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.mapper.AiForecastMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
public class AiForecastServiceImpl extends ServiceImpl<AiForecastMapper, AiForecast> implements AiForecastService {

    @Resource
    private AiForecastMapper aiForecastMapper;

    @Resource
    private AnnotationMapper annotationMapper;


    @Override
    public Boolean forecastResults(Long singleSlideId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);
            if (annotationBy == null) {
                return false;
            }
            AiForecast aiForecast = new AiForecast();
            aiForecast.setSingleSlideId(singleSlideId);
            aiForecast.setResults(annotationBy.getArea());
            aiForecast.setUnit("平方毫米");
            aiForecast.setCreateBy(SecurityUtils.getUserId());
            aiForecast.setCreateTime(String.valueOf(new Date()));
            aiForecastMapper.insert(aiForecast);
            return true;
        } catch (Exception ex) {
            return false;
        }


    }


}




