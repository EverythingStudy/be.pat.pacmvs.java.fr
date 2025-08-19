package cn.staitech.fr.mapper;

import java.util.List;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.out.AiInfoListRequest;
import cn.staitech.fr.vo.project.AiAnalysisBO;
import cn.staitech.fr.vo.project.OrganCheckConfirmBO;
import cn.staitech.fr.vo.project.slide.*;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.staitech.fr.domain.Slide;

import javax.validation.constraints.NotNull;


public interface SlideMapper extends BaseMapper<Slide> {
    String selectBySpecialId(Long specialId);

    CustomPage<SlidePageVo> page(CustomPage page, @Param("params") SlidePageReq req);

    List<SlidePageVo> slideListQuery(@Param("params") SlidePageReq req);

    SlidePageVo slideQueryBy(Long slideId);

    List<Slide> selectListByWax(@Param("topicId") Long topicId, @Param("speciesId")String speciesId);
    
    SlideDetailVo getSlideInfo(Long slideId);

    List<String> selectWaxCodes(@Param("projectId") Long projectId);

    List<SlidePageVo> getSlideSelectList(SlideSelectListReq req);

    boolean isAiSlideFinished(Long projectId);

    List<SlideOrganTagVo> getOrganCode(SlideSelectListReq req);

    List<AiAnalysisBO> selectAiAnalysis(@Param("projectId")Long projectId);

    List<ExportAiInfoVo> exportAiInfo(ExportAiInfoReq req);

    List<AiInfoListVO> getAiInfoList(AiInfoListRequest request);

    List<OrganCheckConfirmBO> selectOrganCheckConfirmBO(Long slideId);

    CustomPage<SlidePageVo> pageNew(CustomPage<SlidePageVo> page, @Param("params") SlidePageReq req);
}




