package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.measure.MeasureSelectPageVo;
import cn.staitech.fr.vo.measure.PointCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Mapper
* @createDate 2024-03-29 10:08:34
* @Entity cn.staitech.fr.domain.Measure
*/
public interface MeasureMapper extends BaseMapper<Measure> {

    int selectListCount(Map<String, Object> map);

    PointCount selectCategoryCount(Measure marking);

    int updatePointCount(Measure marking);

    List<MeasureSelectPageVo> selectPointCountList(Map<String, Object> map);

    List<MeasureSelectPageVo> selectList(Map<String, Object> map);

    List<Features> selectListBy(Long slideId);

    Properties selectBy(Long measureId);

    List<Properties> selectMeasureList(Long slideId);

}




