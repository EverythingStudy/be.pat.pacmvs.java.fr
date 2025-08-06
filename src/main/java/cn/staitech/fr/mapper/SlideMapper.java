package cn.staitech.fr.mapper;

import java.util.List;
import cn.staitech.common.core.domain.CustomPage;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.vo.project.slide.SlidePageReq;
import cn.staitech.fr.vo.project.slide.SlidePageVo;
import cn.staitech.fr.vo.project.slide.SlideDetailVo;

import javax.validation.constraints.NotNull;


public interface SlideMapper extends BaseMapper<Slide> {
    String selectBySpecialId(Long specialId);

    CustomPage<SlidePageVo> page(CustomPage page, @Param("params") SlidePageReq req);

    List<SlidePageVo> slideListQuery(@Param("params") SlidePageReq req);

    SlidePageVo slideQueryBy(Long slideId);

    List<Slide> selectListByWax(@Param("topicId") Long topicId, @Param("speciesId")String speciesId);
    
    SlideDetailVo getSlideInfo(Long slideId);

    List<String> selectWaxCodes(@Param("projectId") Long projectId);
}




