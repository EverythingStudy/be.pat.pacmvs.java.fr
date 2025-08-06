package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.out.AipreAirepostOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author admin
* @description 针对表【fr_ai_forecast】的数据库操作Mapper
* @createDate 2024-04-09 14:42:38
* @Entity cn.staitech.fr.domain.AiForecast
*/
public interface AiForecastMapper extends BaseMapper<AiForecast> {

    AipreAirepostOut getAiForecastBySingle(Long singleId);

}




